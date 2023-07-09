/* IoT Energy Monitoring Plug*/

// Comment this out to disable prints and save space
#define BLYNK_PRINT Serial
#define DHTTYPE DHT11
#define DHTPIN 4 
/* 2. Define the API Key */
#define API_KEY "AIzaSyATRsfMYpXDt6Jw1DnPsIBH4V-3_SCAke4"

/* 3. Define the RTDB URL */
#define DATABASE_URL "https://energy-monitoring-smart-socket-default-rtdb.firebaseio.com/" //<databaseName>.firebaseio.com or <databaseName>.<region>.firebasedatabase.app

/* 4. Define the user Email and password that alreadey registerd or added in your project */
#define USER_EMAIL "apoorvan721@gmail.com"
#define USER_PASSWORD "apoo_rva_N721"


#include "ACS712.h"
#include "ZMPT101B.h"

#include <WiFi.h>
#include <WiFiClient.h>
#include <BlynkSimpleEsp32.h>
#include "DHT.h"
#include <Firebase_ESP_Client.h>
// Provide the token generation process info.
#include <addons/TokenHelper.h>

// Provide the RTDB payload printing info and other helper functions.
#include <addons/RTDBHelper.h>

DHT dht(DHTPIN, DHTTYPE);
FirebaseData fbdo;

FirebaseAuth auth;
FirebaseConfig config;

ACS712 currentSensor(ACS712_30A, 33);
ZMPT101B voltageSensor(34);

#define BLYNK_TEMPLATE_ID "TMPLrASmnQwS"
#define BLYNK_TEMPLATE_NAME "Energy Monitoring Smart Plug"
#define BLYNK_AUTH_TOKEN "V297pCBAsg_O6MbX7AjE_W4S33-c1-V-"

// Your WiFi credentials.
// Set password to "" for open networks.
char ssid[] = "Apoorva";
char pass[] = "9448887766";

void setup() {
  // put your setup code here, to run once:

  Serial.begin(9600);
  dht.begin();
 Blynk.begin(BLYNK_AUTH_TOKEN, ssid, pass);
  Serial.println("IoT Plug");

  pinMode(32, OUTPUT);
  digitalWrite(32, HIGH);

  voltageSensor.autoZeroPoint(50, 4);
  currentSensor.autoZeroPoint(50, 4);
  Serial.printf("Firebase Client v%s\n\n", FIREBASE_CLIENT_VERSION);

  // /* Assign the api key (required) */
  config.api_key = API_KEY;

  // /* Assign the user sign in credentials */
  auth.user.email = USER_EMAIL;
  auth.user.password = USER_PASSWORD;

  // /* Assign the RTDB URL (required) */
  config.database_url = DATABASE_URL;

  // /* Assign the callback function for the long running token generation task */
  config.token_status_callback = tokenStatusCallback; // see addons/TokenHelper.h
  fbdo.setResponseSize(2048);

  Firebase.begin(&config, &auth);
   // Comment or pass false value when WiFi reconnection will control by your code or third party library
  Firebase.reconnectWiFi(true);

  Firebase.setDoubleDigits(5);

  config.timeout.serverResponse = 10 * 1000;

  
voltageSensor.calculatesSensitivity(240);

 voltageSensor.setSensitivity(0.0015);
}

void loop() {
  // put your main code here, to run repeatedly:

Blynk.run();


  float U = voltageSensor.getVoltageAC();
  float I = currentSensor.getCurrentAC();

  // To calculate the power we need voltage multiplied by current
  float P = U * I;
  float h = dht.readHumidity();
  // Read temperature as Celsius (the default)
  float t = dht.readTemperature();
  // Read temperature as Fahrenheit (isFahrenheit = true)
  float f = dht.readTemperature(true);

  // Check if any reads failed and exit early (to try again).
  if (isnan(h) || isnan(t) || isnan(f)) {
    Serial.println(F("Failed to read from DHT sensor!"));
    return;
  }

  // Compute heat index in Fahrenheit (the default)
  float hif = dht.computeHeatIndex(f, h);
  // Compute heat index in Celsius (isFahreheit = false)
  float hic = dht.computeHeatIndex(t, h, false);

  Serial.print(F("Connected to Wifi\n"));
  Serial.print(F("Humidity: "));
  Serial.print(h);
  Serial.print(F("%  Temperature: "));
  Serial.print(t);
  Serial.print(F("°C "));
  Serial.print(f);
  Serial.print(F("°F  Heat index: "));
  Serial.print(hic);
  Serial.print(F("°C "));
  Serial.print(hif);
  Serial.println(F("°F"));

  Serial.println(String("V = ") + U + " V");
  Serial.println(String("I = ") + I + " A");
  Serial.println(String("P = ") + P + " Watts");

  Blynk.virtualWrite(V0, I);
  Blynk.virtualWrite(V1, U);
  Blynk.virtualWrite(V3, P);
  Blynk.virtualWrite(V4, h);
  Blynk.virtualWrite(V5, t);
  
  Serial.printf("Set int... %s\n", Firebase.RTDB.setInt(&fbdo, F("/data/device 1/humidity"), h) ? "ok" : fbdo.errorReason().c_str()); 
  Serial.printf("Set int... %s\n", Firebase.RTDB.setInt(&fbdo, F("/data/device 1/temperature"), t) ? "ok" : fbdo.errorReason().c_str()); 
  Serial.printf("Set int... %s\n", Firebase.RTDB.setInt(&fbdo, F("/data/device 1/current"), I) ? "ok" : fbdo.errorReason().c_str()); 
  Serial.printf("Set int... %s\n", Firebase.RTDB.setInt(&fbdo, F("/data/device 1/voltage"), U) ? "ok" : fbdo.errorReason().c_str()); 
  Serial.printf("Set int... %s\n", Firebase.RTDB.setInt(&fbdo, F("/data/device 1/power"), P) ? "ok" : fbdo.errorReason().c_str()); 
  Serial.printf("Get string... %s\n", Firebase.RTDB.getString(&fbdo, F("/data/device 1/status")) ? fbdo.to<const char *>() : fbdo.errorReason().c_str());
  Serial.println("\n\n");
  Firebase.RTDB.get(&fbdo, F("/data/device 1/status"));
  String str = fbdo.to<String>();
  if(str=="ON")
  {
    Serial.println("DEVICE IS ON");
    digitalWrite(32, HIGH);
  }
  else
  {
    Serial.println("DEVICE IS OFF");
    digitalWrite(32, LOW);
  }
  Serial.printf("Humidity Status... %s\n", Firebase.RTDB.getString(&fbdo, F("/data/device 1/äutomation/humidity/status")) ? fbdo.to<const char *>(): fbdo.errorReason().c_str()); 
  Serial.printf("Humidity Condition... %s\n", Firebase.RTDB.getString(&fbdo, F("/data/device 1/äutomation/humidity/condition")) ? fbdo.to<const char *>(): fbdo.errorReason().c_str());
  Serial.printf("Humidity Value... %s\n", Firebase.RTDB.getString(&fbdo, F("/data/device 1/äutomation/humidity/value"))? fbdo.to<const char *>(): fbdo.errorReason().c_str()); 
  Serial.printf("Device is %s\n",Firebase.RTDB.get(&fbdo, F("/data/device 1/äutomation/humidity/device_status"))? fbdo.to<const char *>() : fbdo.errorReason().c_str());
  String str_humidity = fbdo.to<String>();
  // if(str_humidity=="ON")
  // {
  //   Serial.println("DEVICE IS ON");
  //   digitalWrite(32, LOW);
  // }
  // else
  // {
  //   Serial.println("DEVICE IS OFF");
  //   digitalWrite(32, HIGH);
  // }
  Serial.println("\n\n");
  Serial.printf("Temperature Status... %s\n", Firebase.RTDB.getString(&fbdo, F("/data/device 1/äutomation/temperature/status")) ? fbdo.to<const char *>(): fbdo.errorReason().c_str()); 
  Serial.printf("Temperature Condition... %s\n", Firebase.RTDB.getString(&fbdo, F("/data/device 1/äutomation/temperature/condition")) ? fbdo.to<const char *>(): fbdo.errorReason().c_str());
  Serial.printf("Temperature Value... %s\n", Firebase.RTDB.getString(&fbdo, F("/data/device 1/äutomation/temperature/value")) ? fbdo.to<const char *>(): fbdo.errorReason().c_str());
  Serial.printf("Device is %s\n",Firebase.RTDB.get(&fbdo, F("/data/device 1/äutomation/temperature/device_status"))? fbdo.to<const char *>() : fbdo.errorReason().c_str());
  String str_temperature = fbdo.to<String>();
  // if(str_temperature=="ON")
  // {
  //   Serial.println("DEVICE IS ON");
  //   digitalWrite(32, LOW);
  // }
  // else
  // {
  //   Serial.println("DEVICE IS OFF");
  //   digitalWrite(32, HIGH);
  // }
  // delay(500);
}
