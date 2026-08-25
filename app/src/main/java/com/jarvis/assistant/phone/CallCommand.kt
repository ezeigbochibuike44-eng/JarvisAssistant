package com.jarvis.assistant.phone

import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import androidx.core.content.ContextCompat
import com.jarvis.assistant.ai.CommandResult
import com.jarvis.assistant.ai.JarvisCommand
import com.jarvis.assistant.contacts.ContactsManager

/**
 * "Call John" resolves the contact and asks for confirmation before dialing, matching the
 * two-step flow in the product spec. The router's UI layer is responsible for surfacing
 * the confirmation prompt and re-invoking onConfirm.
 */
class CallCommand(private val context: Context) : JarvisCommand {

    private val contactsManager = ContactsManager(context)

    override fun matches(input: String): Boolean = input.startsWith("call ")

    override suspend fun execute(input: String): CommandResult {
        val name = input.removePrefix("call ").trim()
        if (name.isBlank()) return CommandResult.Error("I need a name to call.")

        if (!contactsManager.hasPermission()) {
            return CommandResult.NeedsPermission(
                permission = Manifest.permission.READ_CONTACTS,
                spokenResponse = "I need Contacts permission before I can find $name."
            )
        }

        val contact = contactsManager.findContact(name)
            ?: return CommandResult.Unsupported("I couldn't find a contact named $name.")

        return CommandResult.NeedsConfirmation(
            spokenResponse = "I found ${contact.name}. Should I call them?",
            onConfirm = { dial(contact.phoneNumber, contact.name) }
        )
    }

    private fun dial(number: String, displayName: String): CommandResult {
        val hasCallPermission = ContextCompat.checkSelfPermission(context, Manifest.permission.CALL_PHONE) == PackageManager.PERMISSION_GRANTED
        if (!hasCallPermission) {
            return CommandResult.NeedsPermission(
                permission = Manifest.permission.CALL_PHONE,
                spokenResponse = "I need Phone permission to place the call."
            )
        }
        val intent = Intent(Intent.ACTION_CALL, Uri.parse("tel:$number")).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        return try {
            context.startActivity(intent)
            CommandResult.Success("Calling $displayName.")
        } catch (e: Exception) {
            CommandResult.Error("The call couldn't be placed.", e)
        }
    }

    override fun describe(input: String): String = input.replaceFirstChar { it.uppercase() }
}
