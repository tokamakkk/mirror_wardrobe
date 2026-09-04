# Plan: Add "Mirror" tab + PortraitCaptureScreen

## Context
Add a "Mirror" AI try-on entry point in the bottom nav bar (center position). When user first taps Mirror, navigate to a new PortraitCaptureScreen for body shape capture. This screen has a 3/4 camera preview area with a shutter button, and a 1/4 bottom sheet with glassmorphism effect showing recent gallery thumbnails. After capture/selecting a photo, user taps "Next" to proceed.

## Implementation Steps

### Step 1: Add CameraX dependency
**File**: `app/build.gradle`

Add CameraX dependencies (consistent with compileSdk 36, minSdk 26):
```gradle
def camerax_version = "1.4.1"
implementation "androidx.camera:camera-core:$camerax_version"
implementation "androidx.camera:camera-camera2:$camerax_version"
implementation "androidx.camera:camera-lifecycle:$camerax_version"
implementation "androidx.camera:camera-view:$camerax_version"
```

### Step 2: Add Mirror route
**File**: `app/src/main/java/com/comp7506/mywardrobe/navigation/AppRoutes.kt`

Add:
```kotlin
data object Mirror : AppRoutes("mirror")
data object PortraitCapture : AppRoutes("portrait_capture")
```

### Step 3: Add Mirror to bottom nav (center position)
**File**: `app/src/main/java/com/comp7506/mywardrobe/MyWardrobeApp.kt`

Insert Mirror item between Wardrobe and Outfits in `bottomNavItems`:
```kotlin
BottomNavItem(
    route = AppRoutes.Mirror.route,
    label = "Mirror",
    selectedIcon = Icons.Filled.ContentCopy, // mirror icon
    unselectedIcon = Icons.Outlined.ContentCopy,
),
```
- Add `AppRoutes.Mirror.route` to `showBottomBar` check
- Add composable route for Mirror (placeholder screen for now, just a text "Mirror")
- Add composable route for PortraitCapture

### Step 4: Create PortraitCaptureScreen
**File**: `app/src/main/java/com/comp7506/mywardrobe/ui/screens/PortraitCaptureScreen.kt`

Layout using Box:
- **Top 75%**: CameraX PreviewView (using `camera-view` artifact's `PreviewView`)
  - Overlay: centered white circle shutter IconButton at bottom of preview area
  - Overlay: top-center text "please capture a full-body photo" (semi-transparent background)
  - Top-left: back arrow IconButton
  - Top-right: "Next" TextButton (enabled only when a photo is selected/captured)
- **Bottom 25%**: Glassmorphism card (semi-transparent white background + blur-like gradient)
  - LazyRow of recent gallery thumbnails loaded via MediaStore ContentResolver
  - Supports tap to select (highlight border)
  - Card can be dragged up to expand (use `Modifier.offset` with draggable state)

CameraX setup:
- Request CAMERA permission on screen entry
- Initialize ProcessCameraProvider, bind Preview + ImageCapture use cases
- Shutter click: call `imageCapture.takePicture()` with output file to cache dir
- On capture success: set selectedImageUri state, dismiss camera (or keep preview)

Gallery thumbnails:
- Use `MediaStore.Images.Media.EXTERNAL_CONTENT_URI` query (content resolver on IO thread)
- Load via Coil `AsyncImage` with `ContentResolver` URI scheme
- Selected state tracked in ViewModel

### Step 5: Create PortraitCaptureViewModel
**File**: `app/src/main/java/com/comp7506/mywardrobe/ui/viewmodel/PortraitCaptureViewModel.kt`

States:
- `selectedImageUri: StateFlow<String?>` — null until user captures/selects
- `galleryThumbnails: StateFlow<List<String>>` — recent images from MediaStore

Functions:
- `selectImage(uri: String)`
- `loadGalleryImages(context: Context)` — query MediaStore on IO thread

Register in `AppViewModelFactory.kt`.

### Step 6: Register ViewModel in factory
**File**: `app/src/main/java/com/comp7506/mywardrobe/ui/viewmodel/AppViewModelFactory.kt`

Add `PortraitCaptureViewModel` creation in the `when` block.

### Step 7: Mirror placeholder screen
**File**: `app/src/main/java/com/comp7506/mywardrobe/ui/screens/MirrorScreen.kt`

Simple screen that checks if user has a captured portrait. If not, navigate to PortraitCapture. Otherwise show "AI try-on" placeholder.

## Critical Files to Modify
- `app/build.gradle` — CameraX deps
- `navigation/AppRoutes.kt` — new routes
- `MyWardrobeApp.kt` — nav bar + routes
- `ui/viewmodel/AppViewModelFactory.kt` — ViewModel registration

## New Files
- `ui/screens/PortraitCaptureScreen.kt`
- `ui/viewmodel/PortraitCaptureViewModel.kt`
- `ui/screens/MirrorScreen.kt`

## Verification
1. Build and run the app
2. Bottom nav should show: Home | Wardrobe | Mirror | Outfits | Calendar (5 items, Mirror in center)
3. Tap Mirror → navigates to PortraitCaptureScreen
4. PortraitCaptureScreen: top 75% shows camera preview with shutter button and "please capture a full-body photo" text
5. Bottom 25% shows glassmorphism card with gallery thumbnails
6. Tap shutter → captures photo, sets selectedImageUri
7. Tap gallery thumbnail → selects that image
8. "Next" button enabled after selection, tap to proceed (placeholder for now)
9. Back arrow returns to previous screen