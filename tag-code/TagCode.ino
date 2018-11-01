#include <AES.h>

#include <RFduinoBLE.h>
#include <SHA512.h>

#include <Ed25519.h>
#include <stdlib.h>
#include <time.h>
#include <stdio.h>
#include <stdlib.h>
#include <string.h>
#include <CBC.h>
#include <AES.h>


const int messageLength = 64;
const int sigLength = 64;
const int publicKeyLength = 32;
const uint8_t tagPrivateKey[32] = /*private key here*/
const uint8_t tagPublicKey[32] = {49,110,136,15,250,165,16,182,202,198,54,169,242,218,115,41,17,230,134,17,240,103,108,54,24,186,126,189,0,114,170,203};
uint8_t serverPublicKeyArr[publicKeyLength] = {209,237,30,219,117,249,88,254,171,149,83,73,251,7,100,63,178,18,105,59,161,59,148,239,108,114,35,171,161,234,247,106};
const uint8_t encryptKey[16] = /*encryptKey here*/
int strConcatId = 0;
uint8_t tagSignature[64]= {};
uint8_t msgAndSigToSend[128] = {};

SHA512 hash;

char hashBuffer[64] = "";
char strConcat[300] = "";
const int numOfChunksToSend = 11;
char chunkToSend[numOfChunksToSend][20] = {0};
char numOfChunksId[11] = {'0','1','2','3','4','5','6','7','8','9','\0'};
unsigned long currentNonce = 0;


void setup() {
  override_uart_limit = true;
  Serial.begin(9600);
  RFduinoBLE.deviceName = "ifs";

  // start the BLE stack
  RFduinoBLE.begin();

}

//RFDUino requires loop to be defined
void loop() {}


void generateSignatureAndHash() {
  char string[11];
  sprintf(string, "%lu", currentNonce);
  strConcat[0] ='\0';
  hash.update(string, strlen(string));
  hash.finalize(hashBuffer, hash.hashSize());
  hash.reset();
  int k=0, j, i;

  Ed25519::sign(tagSignature, tagPrivateKey, tagPublicKey, hashBuffer, 64);
  Serial.println("this is hash buffer");
  for (i=0, j=0; i<messageLength && j<messageLength; i++, j++) {
    msgAndSigToSend[i] = hashBuffer[j];
    Serial.println(msgAndSigToSend[i]);
  }

  Serial.println("this is signature");
  for (i=i, j=0; i<128 && j<messageLength; i++, j++) {
    msgAndSigToSend[i] = tagSignature[j];
    Serial.println(msgAndSigToSend[i]);
  }

}


void encryptTagMsgAndSignature(uint8_t *iv) {
  Serial.println("iv inside encrypt");
  for (int i=0; i<16; i++) {
    Serial.println(iv[i]);
  }
   uint8_t output[128] = {0};
   CBC<AES128> cbc;
   cbc.setKey(encryptKey, 16);
   cbc.setIV(iv, 16);
   cbc.encrypt(output, msgAndSigToSend, 128);
   cbc.clear();
   int i, j, k=0;

  for (i=0; i<6; i++) {
    for (j=0; j<20 && k< 120; j++) {
      chunkToSend[i][j] = output[k];
      k++;
    }
  }
  for (i=0; i<8; i++) {
    chunkToSend[6][i] = output[k];
     k++;
  }

  RFduinoBLE.send(chunkToSend[0], 20);
}

void encryptTagMsgAndSignatureAndFileData(uint8_t *iv, uint8_t *fileData) {
  uint8_t tagSignature[64] = {0};
  Serial.println("encrypt file data one");
  char string[11];
  sprintf(string, "%lu", currentNonce);
  strConcat[0] ='\0';
  hash.update(string, strlen(string));
  hash.finalize(hashBuffer, hash.hashSize());
  hash.reset();

 Serial.println("this is file data");
  for (int i=0; i<64; i++) {
    Serial.println(fileData[i]);
  }

   Ed25519::sign(tagSignature, tagPrivateKey, tagPublicKey, fileData, 64);
   uint8_t message[128] = {0};
   uint8_t output[128] = {0};
   int i, j, k=0;
   for (i=0, j=0; j<64 && i<64; j++, i++) {
     message[i] = hashBuffer[j];
    // Serial.println(message[i]);
   }
   for (i=64, j=0; j<64 && i<128; j++, i++) {
     message[i] = tagSignature[j];
   }

   CBC<AES128> cbc;
   cbc.setKey(encryptKey, 16);
   cbc.setIV(iv, 16);
   cbc.encrypt(output, message, 128);
   cbc.clear();
  for (i=0; i<6; i++) {
    for (j=0; j<20 && k< 120; j++) {
      chunkToSend[i][j] = output[k];
      k++;
    }
  }
  for (i=0; i<8; i++) {
    chunkToSend[6][i] = output[k];
     k++;
  }

  RFduinoBLE.send(chunkToSend[0], 20);
   return;
}

void RFduinoBLE_onConnect()
{
  Serial.println("me connected");
  memset(strConcat, 0, 300);
  strConcatId =0;
}


bool verifyNonce(uint8_t *msg) {
   bool verifiedNonce = true;
   uint8_t hashNonceBuffer[64] = {0};
   char string[11];

   sprintf(string, "%lu", currentNonce);
    Serial.println("current nonce is ");
   Serial.println(currentNonce);
   hash.update(string, strlen(string));
   hash.finalize(hashNonceBuffer, hash.hashSize());
   hash.reset();
   Serial.println("verify nonce");
   size_t n = 64;
   int ret = memcmp(msg, hashNonceBuffer, n);

    if (ret == 0) {
      Serial.println("true");
      return true;
    } else {
       Serial.println("false");
      return false;
    }

}

void RFduinoBLE_onDisconnect()
{
}

bool checkVerified(uint8_t *sigArr, uint8_t *messageArr, int messageLen) {
   bool isVerified = 0;
   return Ed25519::verify(sigArr, serverPublicKeyArr, messageArr, messageLen);
}

bool pubKeyVerified(uint8_t *pubKeyForTagFrmServer) {
   int ret = memcmp(tagPublicKey, pubKeyForTagFrmServer, 32);
    if (ret == 0) {
      Serial.println("true");
      return true;
    } else {
       Serial.println("false");
      return false;
    }
}

void handleMsg (char *msg) {
  //login operations - code(1), iv(16), encrypted of hash of nonce(64), signature(64)
    if (msg[0] == 48) {
      Serial.println("hi i login");
      int i, j =0;
      uint8_t messageArr[113] = {0};
      uint8_t signature[64] = {0};
      uint8_t pubKeyForTagFrmServer[32] = {0};

      for (i=0,j=1; i<32 && j<1+32; i++, j++) {
        pubKeyForTagFrmServer[i] = msg[j];
      }
      for (i=0,j=0; i<1+32+16+64 && j<1+32+16+64; i++, j++) {
        messageArr[i] = msg[j];
        Serial.println(messageArr[i]);
      }
      for (i=0, j=j; i<64 && j<1+32+16+64+64; i++, j++) {
        signature[i] = msg[j];
      }

      if (checkVerified(signature, messageArr, 113) && pubKeyVerified(pubKeyForTagFrmServer)) {
          Serial.println("i verified");
          uint8_t iv[16] = {0};
          uint8_t encryptedArr[64] = {0};
          uint8_t output[64] = {0};
          decryptMsg(iv, encryptedArr, output, messageArr, 64);
          if (verifyNonce(output)) {
            Serial.println("nonce verified");
            generateSignatureAndHash();
            encryptTagMsgAndSignature(iv);
          } else {
            RFduinoBLE.send("!!", 3);
          }
      } else {
          RFduinoBLE.send("!!", 3);
      }
    }
    //uploadFile operations - code(1), pubKey(32), iv(16), encrypted of hash of nonce & hash of filedata(128), signature(64)
    else if (msg[0] == 49) {
      Serial.println("hi i upload file");
      int i, j =0;
      uint8_t messageArr[177] = {0};
      uint8_t signature[64] = {0};
      uint8_t pubKeyForTagFrmServer[32] = {0};

      for (i=0,j=1; i<32 && j<1+32; i++, j++) {
        pubKeyForTagFrmServer[i] = msg[j];
      }
      for (i=0,j=0; i<1+32+16+64+64 && j<1+32+16+64+64; i++, j++) {
        messageArr[i] = msg[j];
      }
      for (i=0, j=j; i<64 && j<1+32+16+64+64+64; i++, j++) {
        signature[i] = msg[j];
      }
      if (checkVerified(signature, messageArr, 177) && pubKeyVerified(pubKeyForTagFrmServer)) {
    //if (checkVerified(signature, messageArr, 145)) {
          Serial.println("i verified");
          uint8_t iv[16] = {0};
          uint8_t encryptedArr[128] = {0};
          uint8_t output[128] = {0};
          decryptMsg(iv, encryptedArr, output, messageArr, 128);
          uint8_t hash[64] = {0};
          uint8_t fileData[64] = {0};
          int i, j=0;
          for (j=0, i=0; j<64 && i<64; j++, i++) {
            hash[j] = output[j];
            Serial.println(hash[j]);
          }
          for(j=0, i=i; j<64 && i<128; j++, i++) {
            fileData[j] = output[i];

          }
          if (verifyNonce(hash)) {
            Serial.println("nonce verified");
            encryptTagMsgAndSignatureAndFileData(iv, fileData);
          } else {
            RFduinoBLE.send("!!", 3);
          }
      } else {
          RFduinoBLE.send("!!", 3);
      }
    } else {
        RFduinoBLE.send("!!", 3);
    }
}

void decryptMsg(uint8_t *iv, uint8_t *encryptedArr, uint8_t *output, uint8_t *msg, int len) {
   int i, j=0;
   Serial.println("this is iv");
   for (j=1+32, i=0; i<16 && j<1+32+16; j++, i++) {
          iv[i] = msg[j];
          Serial.println(iv[i]);
   }
   Serial.println("this is encryptedArr");
   for (j=j, i=0; i<len && j<1+32+16+len; i++, j++) {
       encryptedArr[i] = msg[j];
       Serial.println(encryptedArr[i]);
   }

   CBC<AES128> cbc;
   cbc.setKey(encryptKey, 16);
   cbc.setIV(iv, 16);
   cbc.decrypt(output, encryptedArr, len);
   cbc.clear();
   memset(strConcat, 0, 300);
   strConcatId =0;
}


void RFduinoBLE_onReceive(char *data, int len)
{
     for(int i =0; i<20 ; i++) {
       strConcat[strConcatId] = data[i];
       strConcatId++;
     }

     char stringEnder[3];
     stringEnder[0] = 47;
     stringEnder[1] = 47;
     stringEnder[2] = '\0';

     char *ret = strstr(data, stringEnder);
     Serial.println(data);
      if (strncmp(data, "ACK", strlen("ACK")) == 0) {
      Serial.println(data[3]);
      int numOfChunkId;
        for (int i =0; i<10; i++) {
          if (data[3] == numOfChunksId[i]) {
            numOfChunkId = i+1;
            break;
          }
        }
        RFduinoBLE.send(chunkToSend[numOfChunkId], 20);
       } else if (strcmp(ret, stringEnder)==0) {
        Serial.println("shld be er");
        handleMsg(strConcat);
      }
  }
