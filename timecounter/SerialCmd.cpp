
#include "SerialCmd.h"

SerialCmdClass::SerialCmdClass(){
  mCmdCount = 0;
  mRxLen = 0;
}

SerialCmdClass::~SerialCmdClass(){}

// task
void SerialCmdClass::Task(){

  bool has_cmd = false;

  while (Serial.available() > 0){
    char c = Serial.read();
    if (c == '\n'){
      mRxLen = 0;
    }else{
      mRxStr[mRxLen++] = c;
    }

    if (mRxLen == 4){
      mRxLen = 0;
      uint32_t cmd = pack(mRxStr);
      for (int n = 0; n < SC_N_CMD_TABLE; n++){
	if (mCmdName[n] == cmd){
	  (mCmdFptr[n])();
	  return;
	}
      }
      // cmd not found
      Serial.println("#E no cmd");
    }
  }
}

// ƒRƒ}ƒ“ƒhŠÖ”‚ð“o˜^‚·‚é
bool SerialCmdClass::AddCmd(char* cmd, SerialCmdFptr fptr){

  if (mCmdCount < SC_N_CMD_TABLE){
    mCmdName[mCmdCount] = pack(cmd);
    mCmdFptr[mCmdCount] = fptr;
    mCmdCount++;
    return true;
  }
  return false;
}

uint32_t SerialCmdClass::pack(char* buf){
    uint32_t c = buf[3];
    c = (c << 8) | buf[2];
    c = (c << 8) | buf[1];
    c = (c << 8) | buf[0];
    return c;
}

SerialCmdClass SerialCmd;
