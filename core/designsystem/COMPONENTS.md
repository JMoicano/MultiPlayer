# Design System Components

## Overview

The Design System module (`core/designsystem`) now contains reusable UI components that are shared across all app variants (phone, wear, auto) and feature modules.

## Components

### SearchTextField
**File:** `components/SearchTextField.kt`

A focused search input field with clear button and keyboard management.

**Features:**
- Auto-focus support via `FocusRequester`
- Clear button that appears when text is entered
- Customizable keyboard actions
- Proper focus change callbacks
- Material 3 OutlinedTextField styling

**Usage:**
```kotlin
SearchTextField(
    value = searchQuery,
    onValueChange = { viewModel.updateSearchQuery(it) },
    placeholder = "Search songs...",
    focusRequester = focusRequester,
    onFocusChanged = { isSearchExpanded = it }
)
```

### TrackListItem
**File:** `components/TrackListItem.kt`

Displays a single track in search results with artwork, name, artist, and options menu.

**Features:**
- Album artwork with Coil image loading
- Track and artist names
- More options button
- Click callbacks for interaction
- Responsive design

**Usage:**
```kotlin
TrackListItem(
    track = track,
    onClick = { onTrackClick(track.trackId) },
    onMoreClick = { /* show options */ }
)
```

### State Components
**File:** `components/StateComponents.kt`

Reusable components for common UI states.

#### LoadingIndicator
Shows a circular progress indicator while loading.

```kotlin
LoadingIndicator()
```

#### ErrorMessage
Displays error messages with visual distinction.

```kotlin
ErrorMessage(
    message = "Failed to load songs",
    onRetry = { pagingItems.retry() }
)
```

#### EmptyState
Shows a message when no results are found.

```kotlin
EmptyState()
```

## Architecture

The Design System follows a clean separation of concerns:

```
core/designsystem/
├── components/
│   ├── SearchTextField.kt      # Search input field
│   ├── TrackListItem.kt        # Track list item
│   ├── StateComponents.kt      # Loading, Error, Empty states
│   └── Index.kt                # Package documentation
└── theme/
    ├── Gradient.kt             # Gradient definitions
    ├── Color.kt                # Color palette
    └── Theme.kt                # Material 3 theming
```

## Dependencies

The Design System module depends on:
- `core:network` (for Track model)
- Jetpack Compose
- Material 3
- Coil (image loading)

## Feature Integration

Feature modules (e.g., `feature:search`) import and use Design System components:

```kotlin
import dev.jmoicano.core.designsystem.components.SearchTextField
import dev.jmoicano.core.designsystem.components.TrackListItem
import dev.jmoicano.core.designsystem.components.LoadingIndicator
```

## Adding New Components

When adding new reusable components:

1. Create the component file in `core/designsystem/components/`
2. Follow the existing naming and documentation pattern
3. Ensure proper parameter ordering (required before optional)
4. Update `Index.kt` package documentation
5. Add unit tests in `core/designsystem/src/test/`
6. Document usage examples in KDoc

## Styling

All components use consistent theming:
- **Primary Color:** `#0086A0` (Teal)
- **Dark Background:** `#000000` and `#1A1A1A`
- **Text Color:** `#FFFFFF` (white) and `#999999` (gray)
- **Borders:** `#444444` (dark gray)

Colors can be adjusted in `core/designsystem/theme/Color.kt` if a centralized color system is implemented.

