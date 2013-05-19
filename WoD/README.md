WoD Time Counter
================


windows7 x64でのセットアップ
============================

javaからシリアルポートを使えるようにします。



macでのセットアップ
===================

javaからシリアルポートを使えるようにします。手順は色々あると思いますが、
arduino IDEをすでにインストールしていることを前提にしています。

arduino.appの中に入り、java/**/RXTXcomm.jarを/Library/Java/**/Extensionsにコピーします。

以下のURLにlibrxtxSerial.jnilibがダウンロードします。
http://code.google.com/p/arduino/issues/detail?id=172

これを/Library/Java/**/Extensionsにコピーします。

RxTxSerialが使用する排他制御用のディレクトリを作るため、ターミナルで以下のコマンドを実行します。

 sudo mkdir /var/lock
 sudo chmod 777 /var/lock
