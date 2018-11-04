#include <RFduinoBLE.h>
#include <SHA512.h>
#include <Ed25519.h>
#include <CBC.h>
#include <AES.h>


const int messageLength = 64;
const int sigLength = 64;
const int publicKeyLength = 32;
const uint8_t tagPrivateKey[32] = /*privKey*/
const uint8_t tagPublicKey[32] = /*pubKey*/
uint8_t serverPublicKeyArr[publicKeyLength] = {119,148,42,249,249,244,132,254,196,103,254,160,202,42,77,105,1,57,243,110,208,4,150,21,189,24,57,77,41,121,163,207};
const uint8_t encryptKey[16] = /*encryptKey*/
int strConcatId = 0;
SHA512 hash;

char strConcat[300] = "";
const int numOfChunksToSend = 11;
char chunkToSend[numOfChunksToSend][20] = {0};
char numOfChunksId[11] = {'0','1','2','3','4','5','6','7','8','9','\0'};
unsigned long currentNonce = 0;
unsigned long nonceToSend = 0;


void setup() {
  override_uart_limit = true;
  Serial.begin(9600);
  RFduinoBLE.deviceName = "S0000001I";
  // start the BLE stack
  RFduinoBLE.begin();

}

//RFDUino requires loop to be defined
void loop() {
    RFduino_ULPDelay(INFINITE);
}


void generateSignatureAndHash(uint8_t *tagSignature, uint8_t *msgAndSigToSend) {
  char hashBuffer[64] = "";
  char string[11];
  sprintf(string, "%lu", nonceToSend);
  strConcat[0] ='\0';
  hash.update(string, strlen(string));
  hash.finalize(hashBuffer, hash.hashSize());
  hash.reset();
  int k=0, j, i;

  Ed25519::sign(tagSignature, tagPrivateKey, tagPublicKey, hashBuffer, 64);
  for (i=0, j=0; i<messageLength && j<messageLength; i++, j++) {
    msgAndSigToSend[i] = hashBuffer[j];
  }

  for (i=i, j=0; i<128 && j<messageLength; i++, j++) {
    msgAndSigToSend[i] = tagSignature[j];
  }

}


void encryptTagMsgAndSignature(uint8_t *iv) {
  uint8_t tagSignature[64]= {};
  uint8_t msgAndSigToSend[128] = {};
  uint8_t output[128] = {0};

  generateSignatureAndHash(tagSignature, msgAndSigToSend);

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
  char string[11];
  char hashBuffer[64] = "";
  sprintf(string, "%lu", nonceToSend);
  strConcat[0] ='\0';
  hash.update(string, strlen(string));
  hash.finalize(hashBuffer, hash.hashSize());
  hash.reset();

   Ed25519::sign(tagSignature, tagPrivateKey, tagPublicKey, fileData, 64);
   uint8_t message[128] = {0};
   uint8_t output[128] = {0};
   int i, j, k=0;
   for (i=0, j=0; j<64 && i<64; j++, i++) {
     message[i] = hashBuffer[j];
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
  memset(strConcat, 0, 300);
  strConcatId =0;
}


bool verifyNonce(uint8_t *msg) {
   bool verifiedNonce = true;
   uint8_t hashNonceBuffer[64] = {0};
   char string[11];

   sprintf(string, "%lu", currentNonce);
   hash.update(string, strlen(string));
   hash.finalize(hashNonceBuffer, hash.hashSize());
   hash.reset();
   int ret = memcmp(msg, hashNonceBuffer, 64);

   if (ret == 0) {
     return true;
   } else {
     return false;
   }
}

void RFduinoBLE_onDisconnect() {}

bool checkVerifiedSig(uint8_t *sigArr, uint8_t *messageArr, int messageLen) {
   bool isVerified = 0;
   return Ed25519::verify(sigArr, serverPublicKeyArr, messageArr, messageLen);
}

bool pubKeyVerified(uint8_t *pubKeyForTagFrmServer) {
   int ret = memcmp(tagPublicKey, pubKeyForTagFrmServer, 32);
    if (ret == 0) {
      return true;
    } else {
      return false;
    }
}

void handleMsg (char *msg) {
  //login operations - code(1), iv(16), encrypted of hash of nonce(64), signature(64)
    if (msg[0] == 48) {
      int i, j =0;
      uint8_t messageArr[113] = {0};
      uint8_t signature[64] = {0};
      uint8_t pubKeyForTagFrmServer[32] = {0};

      for (i=0,j=1; i<32 && j<1+32; i++, j++) {
        pubKeyForTagFrmServer[i] = msg[j];
      }
      for (i=0,j=0; i<1+32+16+64 && j<1+32+16+64; i++, j++) {
        messageArr[i] = msg[j];
      }
      for (i=0, j=j; i<64 && j<1+32+16+64+64; i++, j++) {
        signature[i] = msg[j];
      }

      if (checkVerifiedSig(signature, messageArr, 113) && pubKeyVerified(pubKeyForTagFrmServer)) {
          uint8_t iv[16] = {0};
          uint8_t encryptedArr[64] = {0};
          uint8_t output[64] = {0};
          decryptMsg(iv, encryptedArr, output, messageArr, 64);
          if (verifyNonce(output)) {
            nonceToSend=currentNonce++;
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
      if (checkVerifiedSig(signature, messageArr, 177) && pubKeyVerified(pubKeyForTagFrmServer)) {
          uint8_t iv[16] = {0};
          uint8_t encryptedArr[128] = {0};
          uint8_t output[128] = {0};
          decryptMsg(iv, encryptedArr, output, messageArr, 128);
          uint8_t hash[64] = {0};
          uint8_t fileData[64] = {0};
          int i, j=0;
          for (j=0, i=0; j<64 && i<64; j++, i++) {
            hash[j] = output[j];
          }
          for(j=0, i=i; j<64 && i<128; j++, i++) {
            fileData[j] = output[i];

          }
          if (verifyNonce(hash)) {
            nonceToSend=currentNonce++;
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
   for (j=1+32, i=0; i<16 && j<1+32+16; j++, i++) {
       iv[i] = msg[j];
   }
   for (j=j, i=0; i<len && j<1+32+16+len; i++, j++) {
       encryptedArr[i] = msg[j];
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
      if (strncmp(data, "ACK", strlen("ACK")) == 0) {
      int numOfChunkId;
        for (int i =0; i<10; i++) {
          if (data[3] == numOfChunksId[i]) {
            numOfChunkId = i+1;
            break;
          }
        }
        RFduinoBLE.send(chunkToSend[numOfChunkId], 20);
       } else if (strcmp(ret, stringEnder)==0) {
        handleMsg(strConcat);
      }
  }
