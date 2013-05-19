// -*- c++ -*-

////////////////////////////////////////////////////////////
////
//// 仕事時間カウンタ
////

#include "SerialCmd.h"

//// push sw
int psw1 = 12;
int psw2 = 13;

//// ボタン状態
//// 0: 動作開始前, 1: work, 2: not work, 3: stop
int ButtonState = 0;

//// ボタンの状態を返す
//// コマンドフォーマット:
//// RPSW
void cmdRPSW(){
  Serial.print(ButtonState, DEC);
  Serial.print("\n");
}

//// リセット
void cmdRSET(){
  ButtonState = 0;
}

void setup(){
  // シリアルの初期化
  Serial.begin(9600);

  // コマンド関数の追加
  SerialCmd.AddCmd("RPSW", cmdRPSW);
  SerialCmd.AddCmd("RSET", cmdRSET);

  // push swの設定
  pinMode(psw1, INPUT_PULLUP);
  pinMode(psw2, INPUT_PULLUP);
  
}

void loop(){
  SerialCmd.Task();

  // ボタンを監視し、最後に押されたボタンが何かを記録する
  int sw1 = digitalRead(psw1) ^ 1;
  int sw2 = digitalRead(psw2) ^ 1;

  // stop状態から2つのボタンがともに離れたらしばらく停止する
  if (ButtonState == 3){
    if (sw1 == 0 && sw2 == 0){
      ButtonState = 0;
      delay(100);
    }
  }else{
    if (sw1 == 1 && sw2 == 0){		// working
      ButtonState = 1;
    }else if (sw1 == 0 && sw2 == 1){	// not working
      ButtonState = 2;
    }else if (sw1 == 1 && sw2 == 1){	// stop
      ButtonState = 3;
      delay(100);			// チャタリングで状態が1,2になったりするのを防ぐ
    }
  }
}
