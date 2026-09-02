package com.example.ui.screens

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.MenuAnchorType
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.AccountEntity
import com.example.util.AppLanguage
import com.example.util.LocalizationUtil

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TransferDialog(
    accounts: List<AccountEntity>,
    language: AppLanguage,
    useBengaliDigits: Boolean,
    onDismiss: () -> Unit,
    onTransfer: (fromAccount: AccountEntity, toAccount: AccountEntity, amount: Double, note: String) -> Unit
) {
    val isBn = language == AppLanguage.BENGALI

    var fromAccount by remember(accounts) { mutableStateOf(accounts.firstOrNull()) }
    var toAccount by remember(accounts) { mutableStateOf(accounts.getOrNull(1) ?: accounts.firstOrNull()) }
    var amountText by remember { mutableStateOf("") }
    var noteText by remember { mutableStateOf("") }
    var hasError by remember { mutableStateOf(false) }

    var fromExpanded by remember { mutableStateOf(false) }
    var toExpanded by remember { mutableStateOf(false) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = if (isBn) "তহবিল স্থানান্তর (Transfer)" else "Transfer Funds",
                fontWeight = FontWeight.Bold
            )
        },
        text = {
            Column(modifier = Modifier.fillMaxWidth()) {
                // From Account Dropdown
                Text(
                    text = if (isBn) "উৎস অ্যাকাউন্ট (From)" else "From Account",
                    style = MaterialTheme.typography.labelMedium
                )
                Spacer(modifier = Modifier.height(4.dp))
                ExposedDropdownMenuBox(
                    expanded = fromExpanded,
                    onExpandedChange = { fromExpanded = !fromExpanded }
                ) {
                    OutlinedTextField(
                        value = fromAccount?.let {
                            val name = if (isBn) it.nameBn else it.nameEn
                            "$name (${LocalizationUtil.formatMoney(it.balance, useBengaliDigits)})"
                        } ?: "",
                        onValueChange = {},
                        readOnly = true,
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = fromExpanded) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .menuAnchor(MenuAnchorType.PrimaryNotEditable),
                        shape = RoundedCornerShape(10.dp)
                    )
                    ExposedDropdownMenu(
                        expanded = fromExpanded,
                        onDismissRequest = { fromExpanded = false }
                    ) {
                        accounts.forEach { acc ->
                            DropdownMenuItem(
                                text = {
                                    Text(
                                        "${if (isBn) acc.nameBn else acc.nameEn} (${LocalizationUtil.formatMoney(acc.balance, useBengaliDigits)})"
                                    )
                                },
                                onClick = {
                                    fromAccount = acc
                                    fromExpanded = false
                                }
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // To Account Dropdown
                Text(
                    text = if (isBn) "গন্তব্য অ্যাকাউন্ট (To)" else "To Account",
                    style = MaterialTheme.typography.labelMedium
                )
                Spacer(modifier = Modifier.height(4.dp))
                ExposedDropdownMenuBox(
                    expanded = toExpanded,
                    onExpandedChange = { toExpanded = !toExpanded }
                ) {
                    OutlinedTextField(
                        value = toAccount?.let {
                            val name = if (isBn) it.nameBn else it.nameEn
                            "$name (${LocalizationUtil.formatMoney(it.balance, useBengaliDigits)})"
                        } ?: "",
                        onValueChange = {},
                        readOnly = true,
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = toExpanded) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .menuAnchor(MenuAnchorType.PrimaryNotEditable),
                        shape = RoundedCornerShape(10.dp)
                    )
                    ExposedDropdownMenu(
                        expanded = toExpanded,
                        onDismissRequest = { toExpanded = false }
                    ) {
                        accounts.filter { it.id != fromAccount?.id }.forEach { acc ->
                            DropdownMenuItem(
                                text = {
                                    Text(
                                        "${if (isBn) acc.nameBn else acc.nameEn} (${LocalizationUtil.formatMoney(acc.balance, useBengaliDigits)})"
                                    )
                                },
                                onClick = {
                                    toAccount = acc
                                    toExpanded = false
                                }
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Amount
                OutlinedTextField(
                    value = amountText,
                    onValueChange = { amountText = it; hasError = false },
                    label = { Text(if (isBn) "স্থানান্তরের পরিমাণ *" else "Transfer Amount *") },
                    leadingIcon = { Text("৳ ", fontWeight = FontWeight.Bold) },
                    modifier = Modifier.fillMaxWidth().testTag("transfer_amount_input"),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    singleLine = true,
                    shape = RoundedCornerShape(10.dp)
                )

                Spacer(modifier = Modifier.height(8.dp))

                OutlinedTextField(
                    value = noteText,
                    onValueChange = { noteText = it },
                    label = { Text(if (isBn) "নোট (ঐচ্ছিক)" else "Note (Optional)") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    shape = RoundedCornerShape(10.dp)
                )

                if (hasError) {
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = if (isBn) "সঠিক পরিমাণ ও ভিন্ন অ্যাকাউন্ট নির্বাচন করুন" else "Enter valid amount and select different accounts",
                        color = MaterialTheme.colorScheme.error,
                        fontSize = 12.sp
                    )
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val amount = amountText.toDoubleOrNull()
                    val from = fromAccount
                    val to = toAccount
                    if (amount == null || amount <= 0 || from == null || to == null || from.id == to.id) {
                        hasError = true
                        return@Button
                    }
                    onTransfer(from, to, amount, noteText.trim())
                },
                modifier = Modifier.testTag("confirm_transfer_btn")
            ) {
                Text(if (isBn) "স্থানান্তর করুন" else "Transfer")
            }
        },
        dismissButton = {
            OutlinedButton(onClick = onDismiss) {
                Text(if (isBn) "বাতিল" else "Cancel")
            }
        }
    )
}
