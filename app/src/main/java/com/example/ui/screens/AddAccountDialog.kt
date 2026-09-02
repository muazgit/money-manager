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
import com.example.data.model.AccountType
import com.example.util.AppLanguage

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddAccountDialog(
    language: AppLanguage,
    onDismiss: () -> Unit,
    onSave: (nameEn: String, nameBn: String, type: AccountType, initialBalance: Double, accountNumber: String) -> Unit
) {
    val isBn = language == AppLanguage.BENGALI

    val accountTypes = listOf(
        Triple(AccountType.CASH, "নগদ টাকা", "Cash"),
        Triple(AccountType.BKASH, "বিকাশ", "bKash"),
        Triple(AccountType.NAGAD, "নগদ (MFS)", "Nagad"),
        Triple(AccountType.ROCKET, "রকেট", "Rocket"),
        Triple(AccountType.BANK, "ব্যাংক হিসাব", "Bank Account"),
        Triple(AccountType.CARD, "ক্রেডিট/ডেবিট কার্ড", "Credit/Debit Card"),
        Triple(AccountType.SAVINGS, "সঞ্চয় হিসাব", "Savings Account"),
        Triple(AccountType.OTHER, "অন্যান্য ওয়ালেট", "Other Wallet")
    )

    var selectedTypeTuple by remember { mutableStateOf(accountTypes.first()) }
    var customName by remember { mutableStateOf("") }
    var balanceText by remember { mutableStateOf("0") }
    var accNumberText by remember { mutableStateOf("") }
    var expanded by remember { mutableStateOf(false) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = if (isBn) "নতুন অ্যাকাউন্ট / ওয়ালেট যোগ" else "Add New Account",
                fontWeight = FontWeight.Bold
            )
        },
        text = {
            Column(modifier = Modifier.fillMaxWidth()) {
                Text(
                    text = if (isBn) "অ্যাকাউন্টের ধরন" else "Account Type",
                    style = MaterialTheme.typography.labelMedium
                )
                Spacer(modifier = Modifier.height(4.dp))
                ExposedDropdownMenuBox(
                    expanded = expanded,
                    onExpandedChange = { expanded = !expanded }
                ) {
                    OutlinedTextField(
                        value = if (isBn) selectedTypeTuple.second else selectedTypeTuple.third,
                        onValueChange = {},
                        readOnly = true,
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .menuAnchor(MenuAnchorType.PrimaryNotEditable),
                        shape = RoundedCornerShape(10.dp)
                    )
                    ExposedDropdownMenu(
                        expanded = expanded,
                        onDismissRequest = { expanded = false }
                    ) {
                        accountTypes.forEach { item ->
                            DropdownMenuItem(
                                text = { Text(if (isBn) item.second else item.third) },
                                onClick = {
                                    selectedTypeTuple = item
                                    expanded = false
                                }
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                OutlinedTextField(
                    value = customName,
                    onValueChange = { customName = it },
                    label = { Text(if (isBn) "অ্যাকাউন্টের নাম (ঐচ্ছিক)" else "Custom Name (Optional)") },
                    placeholder = { Text(if (isBn) selectedTypeTuple.second else selectedTypeTuple.third) },
                    modifier = Modifier.fillMaxWidth().testTag("account_name_input"),
                    singleLine = true,
                    shape = RoundedCornerShape(10.dp)
                )

                Spacer(modifier = Modifier.height(8.dp))

                OutlinedTextField(
                    value = balanceText,
                    onValueChange = { balanceText = it },
                    label = { Text(if (isBn) "প্রারম্ভিক ব্যালেন্স *" else "Initial Balance *") },
                    leadingIcon = { Text("৳ ", fontWeight = FontWeight.Bold) },
                    modifier = Modifier.fillMaxWidth().testTag("account_balance_input"),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    singleLine = true,
                    shape = RoundedCornerShape(10.dp)
                )

                Spacer(modifier = Modifier.height(8.dp))

                OutlinedTextField(
                    value = accNumberText,
                    onValueChange = { accNumberText = it },
                    label = { Text(if (isBn) "নম্বর / নোট (ঐচ্ছিক)" else "Number / Tag (Optional)") },
                    placeholder = { Text("e.g. 017XXXX, ****4321") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    shape = RoundedCornerShape(10.dp)
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val balance = balanceText.toDoubleOrNull() ?: 0.0
                    val nameEn = if (customName.isNotBlank()) customName.trim() else selectedTypeTuple.third
                    val nameBn = if (customName.isNotBlank()) customName.trim() else selectedTypeTuple.second
                    onSave(nameEn, nameBn, selectedTypeTuple.first, balance, accNumberText.trim())
                },
                modifier = Modifier.testTag("save_account_btn")
            ) {
                Text(if (isBn) "সংরক্ষণ করুন" else "Save")
            }
        },
        dismissButton = {
            OutlinedButton(onClick = onDismiss) {
                Text(if (isBn) "বাতিল" else "Cancel")
            }
        }
    )
}
