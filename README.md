# Description
__Hubtel Payment__ android library allows you to easily integrate payment gateway into your android app and start accepting payments from within your android app.
Hubtel Payment supports Mobile Wallets(MTN, Airtel, Tigo, Vodafone) & Bank Cards payment. You'll need to signup for a merchant account, visit https://unity.hubtel.com/account/signup. 


# Download

__Add a dependency using Gradle:__

Add it in your root build.gradle(Project) at the end of repositories
```java
allprojects {
	repositories {
		...
		maven { url 'https://jitpack.io' }
	}
}
```
	
Add it in your build.gradle(Module)
```java
dependencies {
	...
	compile 'com.github.deluakin:hubtel-payment:v1.3'
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
	<version>v1.2</version>
</dependency>
```

# How do I use this library
Simple use case will look something like this:

```java
try {
	SessionConfiguration sessionConfiguration = new SessionConfiguration()
		.Builder().setClientId("CLIENT-ID")
		.setSecretKey("SECRET-KEY")
		.setEnvironment(Environment.LIVE_MODE)
		.build();
	HubtelCheckout hubtelPayments = new HubtelCheckout(sessionConfiguration);
	hubtelPayments.setPaymentDetails(1.5, "This is a demo payment");
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


# Note
You can set an endpoint url which the payment status and token can be posted(GET) to after payment has been completed.
Hubtel will append "?token=TOKEN&status=STATUS" to your URL. 
The __STATUS__ would either be pending, cancelled or completed depending on whether or not the customer has made payment for the transaction.

```java
SessionConfiguration sessionConfiguration = new SessionConfiguration()
		...
		//token and status will be 
		.setEndPointURL("URL")
		...
```


Make sure to switch the Environment to LIVE_MODE when releasing your app to the public

```java
SessionConfiguration sessionConfiguration = new SessionConfiguration()
		...
		.setEnvironment(Environment.LIVE_MODE)
		...
```