package dev.jakubzika.befair.ui

import androidx.compose.runtime.Composable
import be_fair.app.shared.generated.resources.Res
import be_fair.app.shared.generated.resources.basis_day_adj
import be_fair.app.shared.generated.resources.basis_day_many
import be_fair.app.shared.generated.resources.basis_day_one
import be_fair.app.shared.generated.resources.basis_day_per
import be_fair.app.shared.generated.resources.basis_month_adj
import be_fair.app.shared.generated.resources.basis_month_many
import be_fair.app.shared.generated.resources.basis_month_one
import be_fair.app.shared.generated.resources.basis_month_per
import be_fair.app.shared.generated.resources.basis_week_adj
import be_fair.app.shared.generated.resources.basis_week_many
import be_fair.app.shared.generated.resources.basis_week_one
import be_fair.app.shared.generated.resources.basis_week_per
import be_fair.app.shared.generated.resources.basis_year_adj
import be_fair.app.shared.generated.resources.basis_year_many
import be_fair.app.shared.generated.resources.basis_year_one
import be_fair.app.shared.generated.resources.basis_year_per
import dev.jakubzika.befair.domain.model.ToolBasis
import org.jetbrains.compose.resources.stringResource

/** "Daily", "Weekly", … */
@Composable
fun ToolBasis.adjective(): String = stringResource(
    when (this) {
        ToolBasis.DAY -> Res.string.basis_day_adj
        ToolBasis.WEEK -> Res.string.basis_week_adj
        ToolBasis.MONTH -> Res.string.basis_month_adj
        ToolBasis.YEAR -> Res.string.basis_year_adj
    }
)

/** "per day", "per week", … */
@Composable
fun ToolBasis.per(): String = stringResource(
    when (this) {
        ToolBasis.DAY -> Res.string.basis_day_per
        ToolBasis.WEEK -> Res.string.basis_week_per
        ToolBasis.MONTH -> Res.string.basis_month_per
        ToolBasis.YEAR -> Res.string.basis_year_per
    }
)

/** The unit, singular for exactly one ("1 week") and plural otherwise ("12 weeks"). */
@Composable
fun ToolBasis.unit(count: Int): String = stringResource(
    if (count == 1) {
        when (this) {
            ToolBasis.DAY -> Res.string.basis_day_one
            ToolBasis.WEEK -> Res.string.basis_week_one
            ToolBasis.MONTH -> Res.string.basis_month_one
            ToolBasis.YEAR -> Res.string.basis_year_one
        }
    } else {
        when (this) {
            ToolBasis.DAY -> Res.string.basis_day_many
            ToolBasis.WEEK -> Res.string.basis_week_many
            ToolBasis.MONTH -> Res.string.basis_month_many
            ToolBasis.YEAR -> Res.string.basis_year_many
        }
    }
)
