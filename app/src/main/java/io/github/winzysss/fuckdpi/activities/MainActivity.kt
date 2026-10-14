package io.github.winzysss.fuckdpi.activities

import android.annotation.SuppressLint
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.net.VpnService
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.ListView
import android.widget.ScrollView
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.ActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import androidx.core.content.ContextCompat
import androidx.core.content.edit
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import io.github.winzysss.fuckdpi.R
import io.github.winzysss.fuckdpi.data.*
import io.github.winzysss.fuckdpi.databinding.ActivityMainBinding
import io.github.winzysss.fuckdpi.services.ServiceManager
import io.github.winzysss.fuckdpi.services.TestService
import io.github.winzysss.fuckdpi.services.appStatus
import io.github.winzysss.fuckdpi.utility.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.io.File
import kotlin.system.exitProcess

class MainActivity : BaseActivity() {
    private lateinit var binding: ActivityMainBinding
    private lateinit var historyUtils: HistoryUtils
    private var pendingAutoConnect = false
    private var lastHandledRunId = -1L

    companion object {
        private val TAG: String = MainActivity::class.java.simpleName
        private const val BATTERY_OPTIMIZATION_REQUESTED = "battery_optimization_requested"
        private const val AUTO_STRATEGY_KEY = "auto_strategy"
        private const val AUTO_TEST_DONE_KEY = "auto_test_done"
        const val EXTRA_REQUEST_CONNECT = "io.github.winzysss.fuckdpi.REQUEST_CONNECT"
        private const val KEY_PENDING_AUTO_CONNECT = "pending_auto_connect"
        private const val KEY_LAST_HANDLED_RUN_ID = "last_handled_run_id"

        private fun collectLogs(): String? {
            return try {
                val process = Runtime.getRuntime().exec("logcat *:D -d")
                process.inputStream.bufferedReader().use { reader ->
                    reader.readText()
                }
            } catch (exception: Exception) {
                Log.e(TAG, "Failed to collect logs", exception)
                null
            }
        }
    }

    private val vpnRegister =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) {
            if (it.resultCode == RESULT_OK) {
                ServiceManager.start(this, Mode.VPN)
            } else {
                Toast.makeText(this, R.string.vpn_permission_denied, Toast.LENGTH_SHORT).show()
                updateStatus()
            }
        }

    private val logsRegister = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult(),
        ::handleLogsFileResult
    )

    private fun handleLogsFileResult(result: ActivityResult) {
        if (result.resultCode != RESULT_OK) return
        val data = result.data ?: return
        val uri = data.data
        val path = data.getStringExtra(FileActivity.EXTRA_PATH)
        val file = if (path == null) null else File(path)
        if (uri == null && file == null) return

        lifecycleScope.launch(Dispatchers.IO) {
            val logs = collectLogs()
            if (logs == null) {
                runOnUiThread {
                    Toast.makeText(this@MainActivity, R.string.logs_failed, Toast.LENGTH_SHORT).show()
                }
                return@launch
            }

            try {
                val outputStream = when {
                    uri != null -> contentResolver.openOutputStream(uri)
                    file != null -> file.outputStream()
                    else -> null
                }
                if (outputStream == null) {
                    Log.e(TAG, "Failed to open output stream")
                } else {
                    outputStream.use { stream ->
                        stream.write(logs.toByteArray())
                    }
                }
            } catch (exception: Exception) {
                Log.e(TAG, "Failed to save logs", exception)
            }
        }
    }

    private val receiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            Log.d(TAG, "Received intent: ${intent?.action}")

            if (intent == null) {
                Log.w(TAG, "Received null intent")
                return
            }

            val senderOrd = intent.getIntExtra(SENDER, -1)
            val sender = Sender.entries.getOrNull(senderOrd)
            if (sender == null) {
                Log.w(TAG, "Received intent with unknown sender: $senderOrd")
                return
            }

            when (val action = intent.action) {
                STARTED_BROADCAST,
                STOPPED_BROADCAST -> updateStatus()

                FAILED_BROADCAST -> {
                    Toast.makeText(
                        context,
                        getString(R.string.failed_to_start),
                        Toast.LENGTH_SHORT,
                    ).show()
                    updateStatus()
                }

                else -> Log.w(TAG, "Unknown action: $action")
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        ServiceManager.refresh(this)

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        setupToolbar()

        historyUtils = HistoryUtils(this)

        val intentFilter = IntentFilter().apply {
            addAction(STARTED_BROADCAST)
            addAction(STOPPED_BROADCAST)
            addAction(FAILED_BROADCAST)
        }

        @SuppressLint("UnspecifiedRegisterReceiverFlag")
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            registerReceiver(receiver, intentFilter, RECEIVER_NOT_EXPORTED)
        } else {
            registerReceiver(receiver, intentFilter)
        }

        binding.statusButtonCard.setOnClickListener {
            binding.statusButtonCard.isClickable = false

            val (status, _) = appStatus
            when (status) {
                AppStatus.Halted -> start()
                AppStatus.Running -> stop()
            }

            binding.statusButtonCard.postDelayed({
                binding.statusButtonCard.isClickable = true
            }, 1000)
        }

        binding.statusButtonCard.setOnFocusChangeListener { _, hasFocus ->
            if (hasFocus) {
                binding.statusButtonCard.strokeWidth = 10
                binding.statusButtonCard.strokeColor = android.graphics.Color.argb(100, 0, 0, 0)
            } else {
                binding.statusButtonCard.strokeWidth = 0
            }
        }

        binding.settingsButton.setOnClickListener {
            if (TestService.isRunning) {
                Toast.makeText(this, R.string.test_unavailable, Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            if (appStatus.first == AppStatus.Running) {
                Toast.makeText(this, R.string.settings_unavailable, Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val intent = Intent(this, SettingsActivity::class.java)
            startActivity(intent)
        }

        binding.strategyButton.setOnClickListener {
            showStrategyPicker()
        }

        if (!PermissionUtils.hasNotificationPermission(this)) {
            PermissionUtils.requestNotificationPermission(this, 1)
        } else {
            requestBatteryOptimization()
        }

        lifecycleScope.launch {
            repeatOnLifecycle(androidx.lifecycle.Lifecycle.State.STARTED) {
                TestService.state.collect { state ->
                    renderTestState(state)
                    if (!state.isLoaded && !state.isRunning) {
                        TestService.loadResults(applicationContext)
                    }
                }
            }
        }

        savedInstanceState?.let {
            pendingAutoConnect = it.getBoolean(KEY_PENDING_AUTO_CONNECT, false)
            lastHandledRunId = it.getLong(KEY_LAST_HANDLED_RUN_ID, -1L)
        }

        val requestConnect = intent.getBooleanExtra(EXTRA_REQUEST_CONNECT, false) ||
            getPreferences().getBoolean("auto_connect", false)
        if (!TestService.isRunning && requestConnect &&
            appStatus.first != AppStatus.Running) {
            this.start()
        }

        if (PresetUtils.seedIfNeeded(this)) {
            Toast.makeText(this, R.string.presets_added, Toast.LENGTH_LONG).show()
        }

        ShortcutUtils.update(this)

        if (!needsAutoTest() && getPreferences().getBoolean("update_check_enabled", true)) {
            UpdateChecker.checkAndPrompt(this, lifecycleScope, manual = false)
        }
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        outState.putBoolean(KEY_PENDING_AUTO_CONNECT, pendingAutoConnect)
        outState.putLong(KEY_LAST_HANDLED_RUN_ID, lastHandledRunId)
    }

    private fun needsAutoTest(): Boolean =
        getPreferences().getBoolean(AUTO_STRATEGY_KEY, true) &&
            !getPreferences().getBoolean(AUTO_TEST_DONE_KEY, false)

    private fun renderTestState(state: TestState) {
        if (state.isRunning) {
            binding.statusText.text = getString(R.string.test_process, state.currentStrategy, state.strategies.size)
            binding.statusButtonCard.alpha = 0.6f
        } else {
            binding.statusButtonCard.alpha = 1.0f
            updateStatus()
            onTestFinished(state)
        }
    }

    private fun onTestFinished(state: TestState) {
        if (!state.isLoaded || !pendingAutoConnect || state.runId == lastHandledRunId) return
        lastHandledRunId = state.runId
        pendingAutoConnect = false

        if (state.cancelled) return

        val best = TestService.bestStrategy(state)
        val prefs = getPreferences()
        if (best != null) {
            prefs.edit {
                putBoolean("engine_enable_cmd_settings", true)
                putString("engine_cmd_args", best.command)
                putBoolean(AUTO_TEST_DONE_KEY, true)
            }
            lifecycleScope.launch(Dispatchers.IO) {
                historyUtils.addCommand(best.command)
            }
            updateStrategyButton()
        } else {
            prefs.edit { putBoolean(AUTO_TEST_DONE_KEY, true) }
            Toast.makeText(this, R.string.auto_test_none_found, Toast.LENGTH_LONG).show()
        }

        if (appStatus.first != AppStatus.Running) {
            proceedStart()
        }
    }

    override fun onResume() {
        super.onResume()
        ServiceManager.refresh(this)
        updateStatus()
        updateStrategyButton()
        UpdateChecker.installPending(this)
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        setIntent(intent)
        if (intent.getBooleanExtra(EXTRA_REQUEST_CONNECT, false) &&
            !TestService.isRunning && appStatus.first != AppStatus.Running) {
            this.start()
        }
    }

    override fun onDestroy() {
        runCatching { unregisterReceiver(receiver) }
        super.onDestroy()
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)

        if (requestCode == 1) {
            requestBatteryOptimization()
        }
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.menu_main, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.action_diagnostics -> {
                showDiagnostics()
                true
            }

            R.id.action_save_logs -> {
                val intent = Intent(this, FileActivity::class.java)
                intent.putExtra(FileActivity.EXTRA_MODE, FileActivity.MODE_CREATE)
                intent.putExtra(FileActivity.EXTRA_TYPE, FileActivity.TYPE_LOGS)
                logsRegister.launch(intent)
                true
            }

            R.id.action_close_app -> {
                TestService.stop()
                lifecycleScope.launch {
                    try {
                        TestService.awaitStopped()
                        ServiceManager.waitStop()
                    } catch (exception: Exception) {
                        Log.e(TAG, "Failed to stop service before closing", exception)
                        return@launch
                    }
                    finishAffinity()
                    android.os.Process.killProcess(android.os.Process.myPid())
                    exitProcess(0)
                }
                true
            }

            else -> super.onOptionsItemSelected(item)
        }
    }

    private fun showDiagnostics() {
        val report = DiagnosticUtils.buildReport(this)
        val padding = (24 * resources.displayMetrics.density).toInt()

        val textView = TextView(this).apply {
            text = report
            setPadding(padding, padding / 2, padding, padding / 2)
        }

        val scrollView = ScrollView(this).apply {
            addView(textView)
        }

        val dialog = AlertDialog.Builder(this)
            .setTitle(R.string.diagnostics)
            .setView(scrollView)
            .setPositiveButton(R.string.diagnostic_copy) { _, _ ->
                ClipboardUtils.copy(this, report, getString(R.string.diagnostics))
            }
            .setNegativeButton(android.R.string.cancel, null)
            .create()

        dialog.setOnShowListener {
            val copyButton = dialog.getButton(AlertDialog.BUTTON_POSITIVE)
            val cancelButton = dialog.getButton(AlertDialog.BUTTON_NEGATIVE)
            copyButton.nextFocusLeftId = cancelButton.id
            copyButton.nextFocusRightId = cancelButton.id
            cancelButton.nextFocusLeftId = copyButton.id
            cancelButton.nextFocusRightId = copyButton.id
            copyButton.requestFocus()
        }
        dialog.show()
    }

    private fun start() {
        if (TestService.isRunning) {
            pendingAutoConnect = false
            TestService.stop()
            return
        }
        if (needsAutoTest()) {
            pendingAutoConnect = true
            TestService.start(this, quick = true)
            if (!TestService.isRunning) {
                pendingAutoConnect = false
                updateStatus()
            }
            return
        }
        proceedStart()
    }

    private fun proceedStart() {
        when (getPreferences().mode()) {
            Mode.VPN -> {
                val intentPrepare = VpnService.prepare(this)
                if (intentPrepare != null) {
                    vpnRegister.launch(intentPrepare)
                } else {
                    ServiceManager.start(this, Mode.VPN)
                }
            }

            Mode.Proxy -> ServiceManager.start(this, Mode.Proxy)
        }
    }

    private fun stop() {
        ServiceManager.stop()
    }

    private fun updateStatus() {
        val (status, mode) = appStatus

        Log.i(TAG, "Updating status: $status, $mode")

        val preferences = getPreferences()
        val (ip, port) = preferences.getProxyIpAndPort()

        if (preferences.mode() == Mode.Proxy) {
            binding.proxyAddress.text = getString(R.string.proxy_address, ip, port)
            binding.proxyAddress.visibility = View.VISIBLE
        } else {
            binding.proxyAddress.visibility = View.GONE
        }

        when (status) {
            AppStatus.Halted -> {
                val typedValue = android.util.TypedValue()
                theme.resolveAttribute(android.R.attr.colorPrimary, typedValue,true)
                binding.statusButtonCard.setCardBackgroundColor(typedValue.data)
                binding.statusButtonIcon.clearColorFilter()

                when (preferences.mode()) {
                    Mode.VPN -> {
                        binding.statusText.setText(R.string.vpn_disconnected)
                    }

                    Mode.Proxy -> {
                        binding.statusText.setText(R.string.proxy_down)
                    }
                }
            }

            AppStatus.Running -> {
                binding.statusButtonCard.setCardBackgroundColor(ContextCompat.getColor(this, R.color.green_active))
                binding.statusButtonIcon.setColorFilter(ContextCompat.getColor(this, android.R.color.white))

                when (mode) {
                    Mode.VPN -> {
                        binding.statusText.setText(R.string.vpn_connected)
                    }

                    Mode.Proxy -> {
                        binding.statusText.setText(R.string.proxy_up)
                    }
                }
            }
        }
    }

    private fun updateStrategyButton() {
        val useCmdSettings = getPreferences().getCmdEnable()

        if (!useCmdSettings) {
            binding.strategyButton.visibility = View.GONE
            return
        }

        val pinned = historyUtils.getPinnedHistory()
        val currentCmdArgs = getPreferences().getCmdArgs()

        val matched = pinned.find { it.text == currentCmdArgs }
        val name = matched?.name?.takeIf { it.isNotBlank() }

        binding.strategyButtonText.text = when {
            currentCmdArgs.isBlank() -> getString(R.string.main_strategy_picker)
            name != null -> name
            getPreferences().getBoolean(AUTO_STRATEGY_KEY, true) -> getString(R.string.main_strategy_auto)
            else -> getString(R.string.main_strategy_custom)
        }
        binding.strategyButton.visibility = View.VISIBLE
    }

    private fun showStrategyPicker() {
        val pinned = historyUtils.getPinnedHistory()
        val autoItem = Command(
            text = getString(R.string.auto_strategy_hint),
            pinned = false,
            name = getString(R.string.main_strategy_auto)
        )

        if (pinned.isEmpty() && !getPreferences().getBoolean(AUTO_STRATEGY_KEY, true)) {
            Toast.makeText(this, R.string.main_strategy_no_pinned, Toast.LENGTH_SHORT).show()
            return
        }

        val items = listOf(autoItem) + pinned

        val adapter = object : ArrayAdapter<Command>(
            this,
            R.layout.item_main_strategy,
            items
        ) {
            override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
                val view = convertView ?: layoutInflater.inflate(
                    R.layout.item_main_strategy,
                    parent,
                    false
                )

                val command = getItem(position)!!

                val nameView = view.findViewById<TextView>(R.id.strategyName)
                val textView = view.findViewById<TextView>(R.id.strategyText)
                val dividerView = view.findViewById<View>(R.id.strategyDivider)

                val name = command.name?.takeIf { it.isNotBlank() }

                nameView.visibility = View.VISIBLE
                nameView.text = name ?: getString(R.string.main_strategy_custom)
                textView.maxLines = 1

                textView.text = command.text
                dividerView.visibility = if (position == count - 1) View.GONE else View.VISIBLE

                return view
            }
        }

        val listView = ListView(this).apply {
            divider = null
            setPadding(0, (10 * resources.displayMetrics.density).toInt(), 0, 0)
            this.adapter = adapter
        }

        val dialog = AlertDialog.Builder(this)
            .setTitle(getString(R.string.main_strategy_picker))
            .setView(listView)
            .setNegativeButton(getString(android.R.string.cancel), null)
            .create()

        listView.setOnItemClickListener { _, _, position, _ ->
            if (position == 0) {
                getPreferences().edit {
                    putBoolean(AUTO_STRATEGY_KEY, true)
                    putBoolean(AUTO_TEST_DONE_KEY, false)
                }
                updateStrategyButton()
            } else {
                applyStrategy(items[position].text)
            }
            dialog.dismiss()
        }

        dialog.show()
    }

    private fun applyStrategy(commandText: String) {
        getPreferences().edit {
            putString("engine_cmd_args", commandText)
            putBoolean(AUTO_TEST_DONE_KEY, true)
            putBoolean(AUTO_STRATEGY_KEY, false)
        }

        updateStrategyButton()

        if (appStatus.first == AppStatus.Running) {
            val mode = getPreferences().mode()
            if (mode == Mode.VPN && VpnService.prepare(this) != null) {
                return
            }

            ServiceManager.restart(this, mode)
            Toast.makeText(this, R.string.service_restart, Toast.LENGTH_SHORT).show()
        }
    }

    private fun requestBatteryOptimization() {
        val preferences = getPreferences()
        val alreadyRequested = preferences.getBoolean(BATTERY_OPTIMIZATION_REQUESTED, false)

        if (!alreadyRequested && !PermissionUtils.isBatteryOptimizationDisabled(this)) {
            PermissionUtils.requestBatteryOptimization(this)
            preferences.edit { putBoolean(BATTERY_OPTIMIZATION_REQUESTED, true) }
        }
    }
}
