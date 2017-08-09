# Description
MPower payment android library allow you to easily integrate mpower payment gateway into your android app and start accepting payments from inside your android app.
MPowerPayments supports the following payment methods Mobile Wallets, Credit & Debit Cards, FCB Speedbank Vouchers & Bank Transfers. 
To signup for a merchant account visit https://app.mpowerpayments.com/signup. 
__Note:__ This library is meant to be used by merchants in Ghana.


With this library you will be able to accept the following payment methods within your app:

* __Visa__
* __MasterCard__
* __Mobile Wallet__


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
	compile 'com.github.deluakin:mpower-payment-android:v2.1'
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
	<artifactId>mpower-payment-android</artifactId>
	<version>v2.1</version>
</dependency>
```

# How do I use this library
Simple use case will look something like this:

```java
try {
	SessionConfiguration sessionConfiguration = new SessionConfiguration()
		.Builder().setMasterKey("YOUR_MASTERKEY")
		.setPrivateKey("YOUR_PRIVATEKEY")
		.setToken("YOUR_TOKEN")
		.setEnvironment(Environment.TEST_MODE)
		.build();
	MpowerPayments mpowerPayments = new MpowerPayments(sessionConfiguration);
	mpowerPayments.setPaymentDetails(10, "This is a demo payment");
	mpowerPayments.Pay(this);
	mpowerPayments.setOnPaymentCallback(new OnPaymentResponse() {
		@Override
		public void onFailed(String token, String reason) {
		}

		@Override
		public void onCancelled(String token) {
		}

		@Override
		public void onSuccessful(String token) {
		}
	});
}
catch (MPowerPaymentException e) {
	e.printStackTrace();
}
```


# Note
You can set an endpoint url which the payment status and token can be posted(GET) to after payment has been completed.
MPOWER will append "?token=INVOICE_TOKEN&status=PAYMENT_STATUS" to your URL. 
The __status__ would either be pending, cancelled or completed depending on whether or not the customer has made payment for the transaction.

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