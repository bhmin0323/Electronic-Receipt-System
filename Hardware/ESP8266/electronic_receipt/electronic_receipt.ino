#include <Arduino.h>
#include <ESP8266WiFi.h>
#include <ESP8266HTTPClient.h>
#include <WiFiClient.h>
#include <SSD1306.h>
#include <qrcode.h>
#include <SoftwareSerial.h>

const char* ssid = "dev";
const char* password = "dnflwlq4";

String uploadPath = "http://server.legatalee.me:8000/upload?";
String viewPath = "http://receipt.legatalee.me:8000/view?";

SSD1306 display(0x3c, D2, D1);
QRcode qrcode(&display);

SoftwareSerial RSinput(D7, D8);
SoftwareSerial RSoutput(D5, D6);

const char base64Chars[] = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789+/";

char data[3000] = { 0 };
const int modTable[] = { 0, 2, 1 };

int calcEncodedLength(int inputLength) {
  return ((inputLength + 2) / 3) * 4;
}

void base64Encode(char* output, const char* input, int inputLength) {
  int i = 0, j = 0;
  int encLen = calcEncodedLength(inputLength);
  while (i < inputLength) {
    uint32_t octet_a = i < inputLength ? (unsigned char)input[i++] : 0;
    uint32_t octet_b = i < inputLength ? (unsigned char)input[i++] : 0;
    uint32_t octet_c = i < inputLength ? (unsigned char)input[i++] : 0;
    uint32_t triple = (octet_a << 0x10) + (octet_b << 0x08) + octet_c;
    output[j++] = base64Chars[(triple >> 3 * 6) & 0x3F];
    output[j++] = base64Chars[(triple >> 2 * 6) & 0x3F];
    output[j++] = base64Chars[(triple >> 1 * 6) & 0x3F];
    output[j++] = base64Chars[(triple >> 0 * 6) & 0x3F];
  }
  for (int k = 0; k < modTable[inputLength % 3]; k++) {
    output[encLen - 1 - k] = '=';
  }
  output[encLen] = '\0';
}

void displayLogo() {
  display.clear();
  display.setFont(ArialMT_Plain_16);
  display.setTextAlignment(TEXT_ALIGN_CENTER);
  display.drawString(64, 1, "ELECTRONIC");
  display.drawString(64, 25, "RECEIPT");
  display.drawString(64, 48, "SYSTEM");
  display.display();
}

void setup() {
  pinMode(D3, INPUT_PULLUP);

  Serial.begin(9600);
  RSinput.begin(9600);
  RSoutput.begin(9600);

  display.init();
  display.clear();
  display.display();

  qrcode.init();

  display.setFont(ArialMT_Plain_16);
  display.setTextAlignment(TEXT_ALIGN_CENTER);
  display.drawString(64, 8, "Connecting to");
  display.setFont(ArialMT_Plain_24);
  display.drawString(64, 32, ssid);

  display.display();

  WiFi.begin(ssid, password);
  Serial.println("Connecting");
  while (WiFi.status() != WL_CONNECTED) {
    delay(500);
    Serial.print(".");
  }
  Serial.println("");
  Serial.print("IP Address: ");
  Serial.println(WiFi.localIP());

  display.clear();
  display.setFont(ArialMT_Plain_16);
  display.setTextAlignment(TEXT_ALIGN_CENTER);
  display.drawString(64, 8, "WiFi Connected");
  display.drawString(64, 36, WiFi.localIP().toString());
  display.display();
  delay(1200);

  displayLogo();
}

String input = "";
unsigned long previousMillis = 0;

void loop() {
  while (Serial.available()) {
    // input += (char)Serial.read();
    // delay(3);
    input = Serial.readString();
  }
  while (RSinput.available()) {
    // input += (char)RSinput.read();
    // delay(3);
    input = RSinput.readString();
  }
  if (input != "") {
    input.toCharArray(data, 3000);
    display.clear();
    display.setFont(ArialMT_Plain_16);
    display.setTextAlignment(TEXT_ALIGN_CENTER);
    display.drawString(64, 25, "DATA READ");
    display.display();
    Serial.println(data);

    if (WiFi.status() == WL_CONNECTED) {
      WiFiClient client;
      HTTPClient http;

      String serverPath = uploadPath + "data=";

      int inputLength = strlen(data);
      int encodedLength = calcEncodedLength(inputLength);
      char encodedData[encodedLength + 1];
      base64Encode(encodedData, data, inputLength);

      serverPath += String(encodedData);

      Serial.println(serverPath);

      http.begin(client, serverPath.c_str());

      int httpResponseCode = http.GET();

      if (httpResponseCode > 0) {
        Serial.print("HTTP Response code: ");
        Serial.println(httpResponseCode);
        String payload = http.getString();
        String id = payload.substring(0, 8);
        String hash = payload.substring(9);
        Serial.println(payload);
        Serial.println(id);
        Serial.println(hash);
        String qrPath = viewPath + "id=" + id + "&hash=" + hash;
        qrcode.create(qrPath);
      } else {
        Serial.print("Error code: ");
        Serial.println(httpResponseCode);
      }
      http.end();
    } else {
      Serial.println("WiFi Disconnected");
    }
    delay(300);
    previousMillis = millis();
  }
  while (input != "") {
    yield();
    if (digitalRead(D3) == 0) {
      RSoutput.print(input);
      displayLogo();
      input = "";
    }
    if ((Serial.available()) || (RSinput.available())) {
      displayLogo();
      input = "";
    }
    unsigned long currentMillis = millis();
    if (currentMillis - previousMillis >= 20000) {
      displayLogo();
      input = "";
      previousMillis = millis();
    }
  }
}