#include <RFduinoBLE.h>
#include <SHA512.h>
#include <SHA256.h>
#include <Ed25519.h>
#include <stdlib.h>
#include <time.h>


const int messageLength = 64;
const int sigLength = 64;
const int publicKeyLength = 32;
const uint8_t tagPrivateKey[32] = /*private key here*/
const uint8_t tagPublicKey[32] = {49,110,136,15,250,165,16,182,202,198,54,169,242,218,115,41,17,230,134,17,240,103,108,54,24,186,126,189,0,114,170,203};
uint8_t serverPublicKeyArr[publicKeyLength] = {209,237,30,219,117,249,88,254,171,149,83,73,251,7,100,63,178,18,105,59,161,59,148,239,108,114,35,171,161,234,247,106};
int strConcatId = 0;
char str[2] = "h";

SHA512 hash;
char hashBuffer[64] = "";


char strConcat[170] = "";

const int numOfChunksToSend = 8;
char chunkToSend[numOfChunksToSend][20] = {0};
char numOfChunksId[10] = {'0','1','2','3','4','5','6','7','8','\0'};


void setup() {
  override_uart_limit = true;
  Serial.begin(9600);

  RFduinoBLE.deviceName = "ifs";

  // start the BLE stack
  RFduinoBLE.begin();
}

//RFDUino requires loop to be defined
void loop() {}


void generateSignature() {

  uint8_t tagSignature[64]= "";
  strConcat[0] ='\0';
  hash.update(str, strlen(str));
  hash.finalize(hashBuffer, hash.hashSize());

  //Ed25519::generatePrivateKey(tagPrivateKey);
  //Ed25519::derivePublicKey(tagPublicKey, tagPrivateKey);


  Ed25519::sign(tagSignature, tagPrivateKey, tagPublicKey, hashBuffer, 64);
  int k=0, j, i;

  //for messageHash
  for (i=0; i<4 ; i++) {
    for (j=0; j<20 && k<64; j++) {
      chunkToSend[i][j]= hashBuffer[k];
      k++;
    }
  }
  //for signature
  k=0;
  for (j=4; j<20; j++) {
    chunkToSend[3][j] = tagSignature[k];
    k++;
  }
  for (i=4; i<7; i++) {
     for (j=0; j<20 && k<64; j++) {
        chunkToSend[i][j] = tagSignature[k];
        k++;
     }
  }

}
void RFduinoBLE_onConnect()
{
  Serial.println("me connected");
  generateSignature();
}

void RFduinoBLE_onDisconnect()
{
    Serial.println("me disconnected");
    strcpy(strConcat, "");
    strConcatId =0;
    hash.reset();
}

void checkVerified(uint8_t *sigArr,uint8_t *publicKeyArr, uint8_t *messageArr) {
   bool isVerified = 0;
   isVerified = Ed25519::verify (sigArr, publicKeyArr, messageArr, messageLength);

   if(isVerified) { //then start sending the tag details and signature
    Serial.println("im verified");
    RFduinoBLE.send(chunkToSend[0], 20);
   } else {
    RFduinoBLE.send("!!", 2);
   }

}

void convertToUint8 (char *msg) {
  uint8_t  messageArr[messageLength] = {0};
  uint8_t sigArr[sigLength] = {0};
  int j=0, i=0;

  for (j=0, i=0; j<messageLength && i<messageLength; j++, i++) {
      messageArr[i] = msg[j];
  }

  for (j=j, i=0; j<messageLength+sigLength && i<sigLength; j++,i++) {
      sigArr[i] = msg[j];
   }

  checkVerified(sigArr, serverPublicKeyArr, messageArr);
}

void RFduinoBLE_onReceive(char *data, int len)
{

     for(int i =0; i<strlen(data) ; i++) {
       strConcat[strConcatId] = data[i];
       strConcatId++;
     }

     char stringEnder[3];
     stringEnder[0] = 47;
     stringEnder[1] = 47;
     stringEnder[2] = '\0';

     char *ret = strstr(data, stringEnder);

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
        convertToUint8(strConcat);
      }

  }
