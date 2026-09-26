package dev.jakubzika.befair.ui.screens

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import be_fair.app.shared.generated.resources.Res
import be_fair.app.shared.generated.resources.screen_add_new_item_date_error
import dev.jakubzika.befair.domain.AppResult
import dev.jakubzika.befair.domain.model.CreateItemRequest
import dev.jakubzika.befair.ui.LocalAppContainer
import dev.jakubzika.befair.ui.templates.AddNewItemTemplate
import dev.jakubzika.befair.util.isoToEpochDay
import dev.jakubzika.befair.util.newItemId
import dev.jakubzika.befair.util.todayIso
import kotlin.math.roundToLong
import kotlinx.coroutines.launch
import org.jetbrains.compose.resources.stringResource

/** The item API prices in minor units; the form collects euros. */
private const val CENTS_PER_EURO = 100

@Composable
fun AddNewItemScreen(
    onCreateItem: () -> Unit,
    onBack: () -> Unit
) {
    val container = LocalAppContainer.current
    val scope = rememberCoroutineScope()
    val todayDate = remember { todayIso() }
    val invalidDateError = stringResource(Res.string.screen_add_new_item_date_error)

    var submitting by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    AddNewItemTemplate(
        todayDate = todayDate,
        onBack = onBack,
        submitting = submitting,
        errorMessage = errorMessage,
        onSubmit = { kind, name, category, price, date ->
            val purchasedOn = isoToEpochDay(date)
            if (purchasedOn == null) {
                // Caught before the round-trip: the server rejects a bad date with a 400 anyway.
                errorMessage = invalidDateError
            } else {
                val request = CreateItemRequest(
                    id = newItemId(),
                    kind = kind,
                    name = name,
                    category = category,
                    priceCents = (price * CENTS_PER_EURO).roundToLong(),
                    purchasedOn = purchasedOn
                )

                scope.launch {
                    submitting = true
                    errorMessage = null
                    when (val result = container.itemRepository.addItem(request)) {
                        is AppResult.Success -> onCreateItem()
                        is AppResult.Error -> errorMessage = result.message
                    }
                    submitting = false
                }
            }
        }
    )
}
