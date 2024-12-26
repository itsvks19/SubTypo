package com.teixeira0x.subtypo.ui.common.permission

import android.Manifest
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.provider.Settings
import androidx.activity.result.contract.ActivityResultContracts
import androidx.fragment.app.Fragment
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.teixeira0x.subtypo.ui.common.R
import com.teixeira0x.subtypo.ui.common.utils.permission.checkPermissions

class PermissionResultContract(private val fragment: Fragment) {

  companion object {
    private val permissions: Array<String>
      get() =
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
          arrayOf(Manifest.permission.READ_MEDIA_VIDEO)
        } else {
          arrayOf(
            Manifest.permission.READ_EXTERNAL_STORAGE,
            Manifest.permission.WRITE_EXTERNAL_STORAGE,
          )
        }

    @JvmStatic
    fun Context.isPermissionsGranted(): Boolean {
      return checkPermissions(permissions)
    }
  }

  private val reqPermissions =
    fragment.registerForActivityResult(
      ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
      for (entry in permissions.entries) {
        val result = checkPermission(entry)

        when (result) {
          PermissionResult.DENIED_FOREVER -> {
            showPermissionSettingsDialog()
            break
          }
          PermissionResult.DENIED -> {
            showRequestPermissionDialog()
            break
          }
          else -> Unit
        }
      }
    }

  private fun checkPermission(
    permission: Map.Entry<String, Boolean>
  ): PermissionResult {
    return when {
      fragment.shouldShowRequestPermissionRationale(permission.key) -> {
        PermissionResult.DENIED
      }
      permission.value -> PermissionResult.GRANTED
      else -> PermissionResult.DENIED_FOREVER
    }
  }

  private fun showPermissionSettingsDialog() {
    MaterialAlertDialogBuilder(fragment.requireContext())
      .setTitle(R.string.permission_required)
      .setMessage(R.string.permission_required_settings_detail)
      .setPositiveButton(R.string.permission_required_settings_goto) { _, _ ->
        val intent =
          Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
            data =
              Uri.fromParts(
                "package",
                fragment.requireContext().packageName,
                null,
              )
          }
        fragment.startActivity(intent)
      }
      .setNegativeButton(R.string.no, null)
      .show()
  }

  private fun showRequestPermissionDialog() {
    MaterialAlertDialogBuilder(fragment.requireContext())
      .setTitle(R.string.permission_required)
      .setMessage(R.string.permission_required_detail)
      .setPositiveButton(R.string.grant) { _, _ -> requestPermissions() }
      .setNegativeButton(R.string.no, null)
      .show()
  }

  fun requestPermissions() {
    reqPermissions.launch(permissions)
  }
}
