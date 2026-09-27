package dev.jakubzika.befair.ui.templates

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import be_fair.app.shared.generated.resources.Res
import be_fair.app.shared.generated.resources.screen_add_new_item_back
import be_fair.app.shared.generated.resources.screen_add_new_item_category_label
import be_fair.app.shared.generated.resources.screen_add_new_item_category_placeholder_clothing
import be_fair.app.shared.generated.resources.screen_add_new_item_category_placeholder_tool
import be_fair.app.shared.generated.resources.screen_add_new_item_date_error
import be_fair.app.shared.generated.resources.screen_add_new_item_date_label
import be_fair.app.shared.generated.resources.screen_add_new_item_hint_clothing
import be_fair.app.shared.generated.resources.screen_add_new_item_hint_tool
import be_fair.app.shared.generated.resources.screen_add_new_item_name_error
import be_fair.app.shared.generated.resources.screen_add_new_item_name_label
import be_fair.app.shared.generated.resources.screen_add_new_item_name_placeholder_clothing
import be_fair.app.shared.generated.resources.screen_add_new_item_name_placeholder_tool
import be_fair.app.shared.generated.resources.screen_add_new_item_price_error
import be_fair.app.shared.generated.resources.screen_add_new_item_price_label
import be_fair.app.shared.generated.resources.screen_add_new_item_price_placeholder
import be_fair.app.shared.generated.resources.screen_add_new_item_submit
import be_fair.app.shared.generated.resources.screen_add_new_item_submitting
import be_fair.app.shared.generated.resources.screen_add_new_item_title
import be_fair.app.shared.generated.resources.screen_add_new_item_type_clothing
import be_fair.app.shared.generated.resources.screen_add_new_item_type_label
import be_fair.app.shared.generated.resources.screen_add_new_item_type_tool
import dev.jakubzika.befair.domain.model.ItemKind
import dev.jakubzika.befair.ui.atoms.BeFairDimension
import dev.jakubzika.befair.ui.atoms.BeFairTextField
import dev.jakubzika.befair.ui.atoms.BeFairTheme
import dev.jakubzika.befair.ui.atoms.LocalBeFairExtendedColors
import dev.jakubzika.befair.ui.atoms.PrimaryButton
import dev.jakubzika.befair.ui.molecules.SegmentedControl
import org.jetbrains.compose.resources.stringResource

private val HeaderHeight = 56.dp

// Add Item screen per DESIGN.md: fixed header + back, a labeled Type segmented
// control, name/category/price/date inputs, a type-specific helper hint, and a
// single full-width primary submit. Green is used only on the submit button;
// orange only on field errors. [submitting] disables the button while the create
// request is in flight; [errorMessage] surfaces a form-level failure from the server.
@Composable
fun AddNewItemTemplate(
    todayDate: String,
    onSubmit: (kind: ItemKind, name: String, category: String, price: Double, date: String) -> Unit,
    onBack: () -> Unit,
    modifier: Modifier = Modifier,
    submitting: Boolean = false,
    errorMessage: String? = null
) {
    var kind by remember { mutableStateOf(ItemKind.CLOTHING) }
    var name by remember { mutableStateOf("") }
    var category by remember { mutableStateOf("") }
    var price by remember { mutableStateOf("") }
    var date by remember { mutableStateOf(todayDate) }

    var nameError by remember { mutableStateOf<String?>(null) }
    var priceError by remember { mutableStateOf<String?>(null) }
    var dateError by remember { mutableStateOf<String?>(null) }

    val nameRequiredError = stringResource(Res.string.screen_add_new_item_name_error)
    val priceRequiredError = stringResource(Res.string.screen_add_new_item_price_error)
    val dateRequiredError = stringResource(Res.string.screen_add_new_item_date_error)

    val defaultCategoryClothing = stringResource(Res.string.screen_add_new_item_type_clothing)
    val defaultCategoryTool = stringResource(Res.string.screen_add_new_item_type_tool)

    fun validateAndSubmit() {
        val trimmedName = name.trim()
        val parsedPrice = price.replace(',', '.').toDoubleOrNull()
        val trimmedDate = date.trim()

        val nextNameError = if (trimmedName.isEmpty()) nameRequiredError else null
        val nextPriceError = if (parsedPrice == null || parsedPrice <= 0.0) priceRequiredError else null
        val nextDateError = if (trimmedDate.isEmpty()) dateRequiredError else null

        nameError = nextNameError
        priceError = nextPriceError
        dateError = nextDateError

        if (nextNameError == null && nextPriceError == null && nextDateError == null) {
            val trimmedCategory = category.trim().ifEmpty {
                if (kind == ItemKind.CLOTHING) defaultCategoryClothing else defaultCategoryTool
            }
            onSubmit(kind, trimmedName, trimmedCategory, parsedPrice!!, trimmedDate)
        }
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(HeaderHeight)
        ) {
            IconButton(
                onClick = onBack,
                modifier = Modifier
                    .align(Alignment.CenterStart)
                    .padding(start = BeFairDimension.Spacing.sm)
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = stringResource(Res.string.screen_add_new_item_back),
                    tint = MaterialTheme.colorScheme.onSurface
                )
            }

            Text(
                text = stringResource(Res.string.screen_add_new_item_title),
                style = MaterialTheme.typography.headlineMedium,
                color = MaterialTheme.colorScheme.onSurface,
                modifier = Modifier.align(Alignment.Center)
            )
        }

        HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)

        Column(
            modifier = Modifier
                .weight(1f)
                .verticalScroll(rememberScrollState())
                .padding(BeFairDimension.Spacing.md)
        ) {
            Text(
                text = stringResource(Res.string.screen_add_new_item_type_label).uppercase(),
                style = MaterialTheme.typography.labelLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(modifier = Modifier.height(BeFairDimension.Spacing.xs))
            SegmentedControl(
                options = listOf(
                    stringResource(Res.string.screen_add_new_item_type_clothing),
                    stringResource(Res.string.screen_add_new_item_type_tool)
                ),
                selectedIndex = kind.ordinal,
                onOptionSelected = { index -> kind = ItemKind.entries[index] }
            )

            Spacer(modifier = Modifier.height(BeFairDimension.Spacing.md))

            BeFairTextField(
                modifier = Modifier.fillMaxWidth(),
                value = name,
                onValueChange = { name = it },
                label = stringResource(Res.string.screen_add_new_item_name_label),
                placeholder = if (kind == ItemKind.CLOTHING) {
                    stringResource(Res.string.screen_add_new_item_name_placeholder_clothing)
                } else {
                    stringResource(Res.string.screen_add_new_item_name_placeholder_tool)
                },
                isError = nameError != null,
                errorMessage = nameError
            )

            Spacer(modifier = Modifier.height(BeFairDimension.Spacing.sm))

            BeFairTextField(
                modifier = Modifier.fillMaxWidth(),
                value = category,
                onValueChange = { category = it },
                label = stringResource(Res.string.screen_add_new_item_category_label),
                placeholder = if (kind == ItemKind.CLOTHING) {
                    stringResource(Res.string.screen_add_new_item_category_placeholder_clothing)
                } else {
                    stringResource(Res.string.screen_add_new_item_category_placeholder_tool)
                }
            )

            Spacer(modifier = Modifier.height(BeFairDimension.Spacing.sm))

            BeFairTextField(
                modifier = Modifier.fillMaxWidth(),
                value = price,
                onValueChange = { price = it },
                label = stringResource(Res.string.screen_add_new_item_price_label),
                placeholder = stringResource(Res.string.screen_add_new_item_price_placeholder),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                isError = priceError != null,
                errorMessage = priceError
            )

            Spacer(modifier = Modifier.height(BeFairDimension.Spacing.sm))

            BeFairTextField(
                modifier = Modifier.fillMaxWidth(),
                value = date,
                onValueChange = { date = it },
                label = stringResource(Res.string.screen_add_new_item_date_label),
                placeholder = todayDate,
                isError = dateError != null,
                errorMessage = dateError
            )

            Spacer(modifier = Modifier.height(BeFairDimension.Spacing.xs))

            Text(
                text = if (kind == ItemKind.CLOTHING) {
                    stringResource(Res.string.screen_add_new_item_hint_clothing)
                } else {
                    stringResource(Res.string.screen_add_new_item_hint_tool)
                },
                style = MaterialTheme.typography.labelMedium,
                color = LocalBeFairExtendedColors.current.ink3
            )

            if (errorMessage != null) {
                Spacer(modifier = Modifier.height(BeFairDimension.Spacing.md))
                Text(
                    text = errorMessage,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.error
                )
            }

            Spacer(modifier = Modifier.height(BeFairDimension.Spacing.lg))

            PrimaryButton(
                modifier = Modifier.fillMaxWidth(),
                title = if (submitting) {
                    stringResource(Res.string.screen_add_new_item_submitting)
                } else {
                    stringResource(Res.string.screen_add_new_item_submit)
                },
                isEnabled = !submitting,
                onClick = ::validateAndSubmit
            )
        }
    }
}

@Preview(backgroundColor = 0xF5F5F2, showBackground = true)
@Composable
private fun AddNewItemTemplatePreview() {
    BeFairTheme {
        AddNewItemTemplate(
            todayDate = "2026-09-24",
            onSubmit = { _, _, _, _, _ -> },
            onBack = {}
        )
    }
}
