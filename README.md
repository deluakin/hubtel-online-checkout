## Description
This android library allows you to easily integrate hubtel online checkout into your android app and start accepting payments from within your app.
Hubtel Payment supports Mobile Wallets & Bank Cards payment. You'll need to signup for a [hubtel merchant account](https://unity.hubtel.com/account/signup), also you will need a ClientID and a Secret Key https://unity.hubtel.com/account/api-accounts-add.


## Download

__Add a dependency using Gradle:__

```java
allprojects {
	repositories {
		...
		maven { url 'https://jitpack.io' }
	}
}
```
```java
dependencies {
	...
	implementation 'com.github.deluakin:hubtel-payment:v1.4'
}
```


__Add a dependency using Maven:__
```java
<repositories>
	<repository>
		<id>jitpack.io</id>
		<url>https://jitpack.io</url>
	</repository>
</repositories>
```

```java
<dependency>
	<groupId>com.github.deluakin</groupId>
	<artifactId>hubtel-payment</artifactId>
	<version>v1.4</version>
</dependency>
```

## Usage
Simple use case will look something like this:

```java
try {
	SessionConfiguration sessionConfiguration = new SessionConfiguration()
		.Builder().setClientId("CLIENT-ID")
		.setSecretKey("SECRET-KEY")
		.setMerchantAccountNumber("HUBTEL-ACC-NO")
		.build();
	HubtelCheckout hubtelPayments = new HubtelCheckout(sessionConfiguration);
	hubtelPayments.setPaymentDetails(150.50, "Pepperoni Pizza XL");
	hubtelPayments.Pay(this);
	hubtelPayments.setOnPaymentCallback(new OnPaymentResponse() {
		@Override
		public void onFailed(String token, String reason) {
		}

		@Override
		public void onCancelled() {
		}

		@Override
		public void onSuccessful(String token) {
		}
	});
}
catch (HubtelPaymentException e) {
	e.printStackTrace();
}
```



## FAQ
__How do i get a hubtel merchant account?__
Visit https://unity.hubtel.com/account/signup to create one

__How do i get a Client Id and Secret Key?__
You will need to register an App. To do that, go to this url https://unity.hubtel.com/account/api-accounts-add. Make sure the "API TYPE" selected is "HTTP REST API". After registering an App, Hubtel will automatically generate a Client ID and a Client Secret.