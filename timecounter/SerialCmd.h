// -*- c++ -*-

#ifndef SERIALCMD_H
#define SERIALCMD_H

#include "arduino.h"
#include <inttypes.h>

// cmd tableに登録できるコマンドの数
#define SC_N_CMD_TABLE 8

typedef void (*SerialCmdFptr)();

class SerialCmdClass {
protected:
  uint32_t mCmdName[SC_N_CMD_TABLE];
  SerialCmdFptr mCmdFptr[SC_N_CMD_TABLE];
  uint8_t mCmdCount;

  char mRxStr[4];
  uint8_t mRxLen;

  uint32_t pack(char* buf);

public:
  SerialCmdClass();
  ~SerialCmdClass();

  // loop()の中で定期的にコールすること
  void Task();

  // コマンド関数を登録する
  bool AddCmd(char* cmd, SerialCmdFptr fptr);
};

extern SerialCmdClass SerialCmd;

#endif	// !SERIALCMD_H
