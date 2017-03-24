=============
Program Usage
=============
Usage:
  java Server/Main <map> <classPath> <player1> <player2> ...

Example:
  java Server/Main defaultMap.txt ./ ContohAI1 ContohAI2
  (Compile `ContohAI1.java` and `ContohAI2.java` first!)

Where:
  * <map> is a text file that contains the map.
  * <classPath> is the location of AI .class files relative to this directory.
  * <playerN> is the AI-program class-name for player-N.


=========
Objective
=========
Jadilah last man standing. ATAU jika hal tersebut tidak terjadi
dalam 1000 turns, jadilah pemain yang memiliki poin terbanyak.

================
Breakdown points
================
>> Poin bunuh player: 500
   * Dilihat dari bom-nya siapa yang ngebunuh player itu
   * Bunuh diri poinnya nggak dihitung
>> Poin ngambil powerup: 50
>> Poin ancurin balok: 10

==========
Game rules
==========
[1] Semua player di-spawn pada suatu cell tertentu di map
    State awal: bomb-count=1, bomb-range=1
    (bomb-count adalah jumlah bom si player itu yang bisa berada di papan dalam suatu waktu.
    taruh 1 bomb: <bomb-count> - 1; bomb-nya udah meledak 1: <bomb-count> + 1)
[2] Action tiap turn: gerak satu langkah ke U/D/L/R, diam, atau taruh bom
[3] Bom yang ditaruh punya data (1) siapa yang naruh, (2) kekuatannya berapa, (3) berapa turn lagi
    bakal meledak. Setiap bomb yang ditaruh punya count:8 (baru kelihatan di input buat turn berikutnya).
    Setiap turn, count-nya berkurang 1. Kalau count-nya udah sampai 1, next turn dia akan meledak.
[4] 2 atau lebih player bisa berada di cell yang sama. Kalau ada 2 player yang naruh bom barengan di
    cell tersebut, bomb yang di-spawn tetep cuma 1 (tapi dianggap milik mereka bersama, jadi masing-
    masing kehilangan <bomb-count>, tapi kalau udah meledak masing-masing dapat <bomb-count>-nya lagi).
    Bomb-yang di-spawn tersebut ngambil bomb-range tertinggi dari player-player yang naruh bomb tersebut.
    {NOTE: data kepemilikan bom nggak dimunculin di input, cuma disimpan di server}
[5] Ilustrasi mekanisme BOM meledak: (.)=kosong, (#)=tembok-keras, (x)=tembok-biasa, (F)=Flare
    Asumsikan BOM punya kekuatan 2. Perhatikan kalau ledakan 1 bom nggak bisa tembus multiple tembok-biasa
    [.][.][.][x][.][.][.]    [.][.][.][x][.][.][.]    [.][.][.][x][.][.][.]
    [#][#][#][x][.][.][.]    [#][#][#][F][.][.][.]    [#][#][#][.][.][.][.]
    [.][.][.][B][.][#][.] -> [.][F][F][F][F][#][.] -> [.][.][.][.][.][#][.]
    [.][.][.][.][.][.][.]    [.][.][.][F][.][.][.]    [.][.][.][.][.][.][.]
    [.][.][.][.][.][.][.]    [.][.][.][F][.][.][.]    [.][.][.][.][.][.][.]
[6] Di daerah di mana bom meledak, bakal muncul Flare yang stays di field selama 2 putaran.
    Jadi urutannya: [Bomb/count:2] -> [Bomb/count:1] -> [Flare/time:2] -> [Flare/time:1] -> [Kosong]
    Kalau kebetulan ada bom yang kena flare, maka secara otomatis count-nya akan jadi 1 (langsung meledak
    next turn). Kalau ada kotak flare kena flare lagi, maka nilai <time>-nya ngambil yang tertinggi (max=2)

============
How it works
============
>> Setiap player membuat program AI
>> Setiap turn:
   1) Program AI player membaca board state dari stdin (baca "sketch input" di bawah)
   2) Program AI player mengeluarkan move turn tersebut di stdout.
>> Possible output/move:
   CATATAN-1: awalan ">>" bersifat wajib, baris tanpa ">>" akan diabaikan (bisa untuk debug output)
   CATATAN-2: jika move yang dibuat tidak valid, sistem menganggap "STAY"
   CATATAN-3: move harus dikeluarkan dalam satu detik setelah input diterima, atau dianggap timeout.
   * ">> STAY"
   * ">> MOVE LEFT"
   * ">> MOVE RIGHT"
   * ">> MOVE UP"
   * ">> MOVE DOWN"
   * ">> DROP BOMB"

============
Sketch input
============
[1] Baris pertama:
    TURN <turn-ke-berapa>
[2] Baris selanjutnya:
    PLAYER <N=jumlah-player>
[3] N barus berikutnya:
    <player-index> <player-id> Bomb:<bomb-bisa-ditaruh>/<max> Range:<bomb-power> <status> <score>
[4] Baris berikutnya:
    BOARD <H=tinggi> <L=lebar>
[5] H baris berikutnya:
    <representasi-board>
[6] Satu baris berikutnya:
    END
%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%
TURN 0
PLAYER 4
P0 1306382032 Bomb:1/1 Range:2 Active 0
P1 1306382032 Bomb:0/1 Range:3 Dead 0
P2 1306382032 Bomb:1/1 Range:2 Dead 0
P3 1306382032 Bomb:1/1 Range:2 Active 10
BOARD 11 13
[ ### ][ ### ][  1  ][     ][     ][     ][     ][     ][     ][     ][     ][     ][     ]
[ ### ][     ][     ][     ][     ][     ][     ][     ][     ][     ][     ][     ][     ]
[ ### ][     ][     ][     ][     ][     ][     ][     ][     ][     ][     ][     ][     ]
[ ### ][ ### ][     ][     ][     ][     ][     ][     ][     ][     ][     ][     ][     ]
[ ### ][     ][     ][     ][     ][     ][     ][     ][     ][     ][     ][     ][     ]
[ ### ][     ][     ][     ][     ][     ][     ][     ][     ][     ][     ][     ][     ]
[ ### ][ ### ][     ][     ][     ][ B21 ][     ][     ][     ][     ][     ][     ][     ]
[ ### ][     ][     ][     ][     ][     ][     ][     ][     ][     ][     ][     ][     ]
[ ### ][     ][     ][     ][     ][     ][     ][     ][     ][     ][     ][     ][     ]
[ ### ][ ### ][     ][     ][     ][     ][     ][     ][     ][     ][     ][     ][     ]
[ ### ][     ][     ][     ][     ][     ][     ][     ][     ][     ][     ][     ][     ]
[ ### ][  2  ][     ][     ][     ][     ][     ][     ][     ][     ][     ][     ][     ]
[ ### ][ ### ][     ][     ][  0  ][     ][     ][     ][     ][     ][     ][     ][     ]
END
%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%

=====================
Keterangan buat input
=====================
[1] Kalau isinya kurang dari lima karakter, akan ada padding dengan spasi (hanya untuk visual)
    (intinya spasi merupakan karakter yang tidak penting, bisa dibuang dulu spasinya sebelum di-parse)
[2] Kalau ada lebih dari 2 brang di satu tempat, bakal dipisahin titik-koma, contoh:
    [ ### ][  1  ][2;4;3][ ### ]
    [     ][ XXX ][F2;+B][     ]
    [     ][     ][ ### ][ XBX ]
[3] Keterangan apa-apa aja yang mungkin ada di setiap cell:
    * [     ]: None
    * [  0  ]: Player 0
    * [ B21 ]: Bomb power:2/count:1 -> (pas ditaruh count:8; kalau udah count:1 next turn meledak)
               {Power bisa jadi dua digit, e.g. B208 -> artinya power:20/count:8}
    * [ F2  ]: Flare time:2 -> (pas bomb meledak time:2; kalau udah time:1; next turn hilang)
    * [ ### ]: Indestructible wall
    * [ XXX ]: Destructible wall
    * [ +B  ]: Powerup Bomb+ (jumlah bomb yang bisa ditaruh meningkat)
    * [ +P  ]: Powerup Power+ (jumlah power/range bomb meningkat 1)
    * [ XBX ]: Destructible wall with Bomb+ powerup inside
    * [ XPX ]: Destructible wall with Power+ powerup inside

=================
Untuk membuat map
=================
>> Karakter-karakter yang bisa dipakai:
   0: P0 spawn point
   1: P1 spawn point
      ... (so on and so forth)
   #: Indestructible wall
   X: Destructible wall
   B: Destructible wall with Bomb+
   P: Destructible wall with Power+
   .: Empty cell
>> Contohnya seperti di bawah (tidak perlu specify length/width)
>> Dalam 1 map, max 10 players: P0..P9
%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%
.X.X..
.###..
.0..1.
......
%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%