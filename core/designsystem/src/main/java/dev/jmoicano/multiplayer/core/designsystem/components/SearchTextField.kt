package dev.jmoicano.multiplayer.core.designsystem.components

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.text.input.ImeAction
import dev.jmoicano.multiplayer.core.designsystem.theme.DesignSystem

/**
 * Reusable search text field component for song search.
 *
 * Styled using [DesignSystem] tokens for consistent theming.
 *
 * @param value Current search query value
 * @param onValueChange Callback when query text changes
 * @param placeholder Placeholder text shown when the field is empty
 * @param leadingIcon Optional painter for the leading icon
 * @param onSearchSubmit Callback when the search IME action is triggered
 * @param modifier Modifier for the text field
 * @param focusRequester FocusRequester for programmatic focus control
 * @param onFocusChanged Callback notified when focus state changes
 */
@Composable
fun SearchTextField(
    value: String,
    onValueChange: (String) -> Unit,
    placeholder: String = "Search songs...",
    leadingIcon: Painter? = null,
    onSearchSubmit: () -> Unit = {},
    modifier: Modifier = Modifier,
    focusRequester: FocusRequester = FocusRequester(),
    onFocusChanged: (Boolean) -> Unit = {},
) {
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        modifier = modifier
            .fillMaxWidth()
            .focusRequester(focusRequester)
            .onFocusChanged { onFocusChanged(it.isFocused) },
        placeholder = {
            Text(
                text = placeholder,
                style = DesignSystem.typography.bodyLarge,
                color = DesignSystem.colors.textSecondary,
            )
        },
        leadingIcon = leadingIcon?.let {
            {
                Icon(
                    painter = it,
                    contentDescription = null,
                    modifier = Modifier.size(DesignSystem.sizing.iconSizeSmall),
                    tint = DesignSystem.colors.alphaInvert25,
                )
            }
        },
        trailingIcon = if (value.isNotEmpty()) {
            {
                IconButton(onClick = { onValueChange("") }) {
                    Icon(
                        imageVector = Icons.Default.Clear,
                        contentDescription = "Clear",
                        tint = DesignSystem.colors.alphaInvert25,
                    )
                }
            }
        } else null,
        shape = RoundedCornerShape(DesignSystem.sizing.cornerRadiusSmall),
        colors = OutlinedTextFieldDefaults.colors(
            focusedContainerColor = DesignSystem.colors.alphaInvert10,
            unfocusedContainerColor = DesignSystem.colors.alphaInvert10,
            focusedTextColor = DesignSystem.colors.textPrimary,
            unfocusedTextColor = DesignSystem.colors.textPrimary,
            cursorColor = DesignSystem.colors.primary,
            focusedBorderColor = Color.Transparent,
            unfocusedBorderColor = Color.Transparent,
        ),
        singleLine = true,
        textStyle = DesignSystem.typography.bodyLarge,
        keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
        keyboardActions = KeyboardActions(onSearch = { onSearchSubmit() }),
    )
}
