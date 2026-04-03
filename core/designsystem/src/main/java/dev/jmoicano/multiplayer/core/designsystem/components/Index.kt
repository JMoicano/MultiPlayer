/**
 * Core Design System Components Package
 *
 * This package contains all reusable UI components used throughout the MultiPlayer application.
 * Every component relies exclusively on [dev.jmoicano.multiplayer.core.designsystem.theme.DesignSystem]
 * tokens — no hardcoded colours or dimensions.
 *
 * ## Components
 * - [TrackListItem]: Track row used in search results and queues
 * - [SearchTextField]: Search input with focus management and clear button
 * - [StandardTopAppBar]: Themed top app bar shared across all screens
 * - [AlbumHeader]: Artwork + title/subtitle header (supports portrait & landscape)
 * - [PlaybackControls]: Seek bar + transport control buttons for the Player screen
 * - [PlayerSidePanel]: Side panel with lazy content list for expanded layouts
 * - [ActionSheetTrackInfo]: Track name/artist header inside a bottom sheet
 * - [ActionSheetMenuItem]: Icon + label menu row inside a bottom sheet
 * - [LoadingIndicator]: Full-screen circular progress indicator
 * - [PaginationLoadingIndicator]: Compact loading indicator for lazy list pagination
 * - [ErrorMessage]: Error state with retry button
 * - [EmptyState]: Empty search results message
 */
package dev.jmoicano.multiplayer.core.designsystem.components
