<h1 align="center">
    Native Android SDK For FACEKI V2
</h1>

<p align="center">
  <strong>Faceki V2 Know Your Customer</strong><br>
</p>

## Client Id and Client Secret:

To obtain a client ID and secret for using our API, you will need to create an account on the Faceki dashboard by visiting the following URL: https://apps.faceki.com/.

# Tutorial For Client ID and Secret:

URL: https://kycdocv2.faceki.com/quick-guides/integration-setting

Once you have created an account, navigate to the "Integrations" section of the dashboard and click on the "Client ID" option.




# Getting Started:


## General Requirements
The minimum requirements for the SDK are:
*	Android 5.0 (API level 21) or higher
*	Internet connection


## Permissions
Required permissions are linked automatically by the SDK.


## Integration by sdk
Use the SDK in your application by including the Maven repositories with the following `build.gradle` configuration in Android Studio:

```
repositories {
	...
	maven { url 'https://jitpack.io' }
}
```


## Integration by library
Use the Library in your application by implemention lib with the following `build.gradle` project module in Android Studio:

```
	implementation 'com.github.faceki:faceki-kyc-android-revamp:Tag'

```
or

```
    implementation("com.github.faceki:faceki-kyc-android-revamp:Tag")

```

and including the Maven repositories with the following `build.gradle` configuration in Android Studio:

```
repositories {
	...
	maven { url 'https://jitpack.io' }
}
```

## Example


```
import com.faceki.android.FaceKi
```

## Kotlin

```
FaceKi.startKycVerification(
                context = this@MainActivity,
                clientId = "clientId",
                clientSecret = "clientSecret",
                kycResponseHandler = kycResponseHandler
            )

//custom logo
FaceKi.setCustomIcons(
                iconMap = hashMapOf(
                    FaceKi.IconElement.Logo to FaceKi.IconValue.Resource(R.drawable.logo)
                )
            )

//custom colors
FaceKi.setCustomColors(
                colorMap = hashMapOf(
                    FaceKi.ColorElement.BackgroundColor to FaceKi.ColorValue.StringColor("#FFFFFF")
                )
            )
```
##### To Get the response back from the SDK.


```
private val kycResponseHandler: KycResponseHandler = object : KycResponseHandler {
        override fun handleKycResponse(
            json: String?,
            type: VerificationType,
            result: VerificationResult
        ) {
            when (result) {
                is VerificationResult.ResultOk -> {}
                is VerificationResult.ResultCanceled -> {}
            }

            when (type) {
                is VerificationType.SingleKYC -> {}
                is VerificationType.MultipleKYC -> {}
            }
        }
    }
```

## Java


```
FaceKi.startKycVerification(
                MainActivity.this,
                "clientId",
                "clientSecret",
                kycResponseHandler
        );

//custom logo
HashMap<FaceKi.IconElement, FaceKi.IconValue> iconMap = new HashMap<>();
        iconMap.put(FaceKi.IconElement.Logo, new FaceKi.IconValue.Resource(R.drawable.logo));
        FaceKi.setCustomIcons(iconMap);

//custom colors
HashMap<FaceKi.ColorElement, FaceKi.ColorValue> colorMap = new HashMap<>();
        colorMap.put(FaceKi.ColorElement.BackgroundColor, new FaceKi.ColorValue.StringColor("#FFFFFF"));
        FaceKi.setCustomColors(colorMap);

```
##### To Get the response back from the SDK

```
private final KycResponseHandler kycResponseHandler = new KycResponseHandler() {
        @Override
        public void handleKycResponse(@Nullable String json, @Nullable VerificationType type, @NonNull VerificationResult result) {
            if (result instanceof VerificationResult.ResultOk) {
                // Handle ResultOk
            } else if (result instanceof VerificationResult.ResultCanceled) {
                // Handle ResultCanceled
            }


            if (type instanceof VerificationType.SingleKYC) {
                // Handle SingleKYC
            } else if (type instanceof VerificationType.MultipleKYC) {
                // Handle MultipleKYC
            }

        }
    };
```

# Custom colors

Use `setCustomColors` to customize the color scheme.

## Kotlin

```
val colorMap = hashMapOf(
    FaceKi.ColorElement.ButtonBackgroundColor to FaceKi.ColorValue.IntColor(myColorInt),
    // Add other elements as needed
)
FaceKi.setCustomColors(colorMap)
```

## Java

```
HashMap<FaceKi.ColorElement, FaceKi.ColorValue> colorMap = new HashMap<>();
        colorMap.put(FaceKi.ColorElement.BackgroundColor, new FaceKi.ColorValue.StringColor("#FFFFFF"));
        FaceKi.setCustomColors(colorMap);
```

# Custom logo

Use `setCustomIcons` to customize the icons.

## Kotlin

```
val iconMap = hashMapOf(
    FaceKi.IconElement.Logo to FaceKi.IconValue.Resource(myDrawableResId),
    // Add other elements as needed
)
FaceKi.setCustomIcons(iconMap)
```

## Java

```
HashMap<FaceKi.IconElement, FaceKi.IconValue> iconMap = new HashMap<>();
        iconMap.put(FaceKi.IconElement.Logo, new FaceKi.IconValue.Resource(R.drawable.ic_launcher_background));
        FaceKi.setCustomIcons(iconMap);
```


### Response Handling

- The response from KYC verification is a plain JSON object.
- You can convert this response into a JSON object using serialization libraries like Gson or Moshi.

## Methods

- **startKycVerification**: Initiates the KYC verification process.
- **setCustomColors**: Customizes the colors of various UI elements.
- **setCustomIcons**: Customizes the icons used in the UI.

## Enums and Sealed Classes

### ColorElement

- Enum defining different UI elements that can have their colors customized.

### ColorValue

- Sealed class representing a color value.
- Types:
    - `IntColor`: Represents color as an integer.
    - `StringColor`: Represents color as a string (e.g., "#FFFFFF").

### IconElement

- Enum defining different UI elements that can have their icons customized.

### IconValue

- Sealed class representing an icon value.
- Types:
    - `Resource`: Represents an icon as a resource ID.
    - `Url`: Represents an icon as a URL.