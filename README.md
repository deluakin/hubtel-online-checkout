[![GitHub issues](https://img.shields.io/github/issues/deluakin/hubtel-online-checkout.svg)](https://github.com/deluakin/hubtel-online-checkout/issues)
[![GitHub forks](https://img.shields.io/github/forks/deluakin/hubtel-online-checkout.svg)](https://github.com/deluakin/hubtel-online-checkout/network)
[![GitHub stars](https://img.shields.io/github/stars/deluakin/hubtel-online-checkout.svg)](https://github.com/deluakin/hubtel-online-checkout/stargazers)
[![GitHub license](https://img.shields.io/github/license/deluakin/hubtel-online-checkout.svg)](https://github.com/deluakin/hubtel-online-checkout/blob/master/LICENSE)


## Description
This android library allows you to easily integrate hubtel online checkout into your android app and start accepting payments from within your app.
Hubtel Payment supports Mobile Wallets & Bank Cards payment. You'll need to [signup](https://hubtel.com) for a merchant account.


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
	implementation 'com.github.deluakin:hubtel-online-checkout:v1.4'
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
	<artifactId>hubtel-online-checkout</artifactId>
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
