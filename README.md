## Synopsis

    Android application that uses the yts.re API to fetch the newest movies in DVD or BR quality. 
    You have the option to download the torrent and remotely sync or send it to your computer 
    using BitTorrent Remote or BitTorrent Sync.

## Usage

    #BitTorrent Remote
    
    The default download method is BitTorrent Remote.
    This requires you to download BitTorrent Remote on your smartphone.
    Next you must install BitTorrent Client on you computer.
    Activate remote usage on the client and you are ready to go.
    BUT when you download with BitTorrent Remote, both your computer and BitTorrent Client must be running.
    
    #BitTorrent Sync
    
    The second option, and in my opinion the best one, is BitTorrent Sync.
    This requires you to download BitTorrent Sync on your smathphone.
    Next you must install BitTorrent Client AND BitTorrent Sync on you computer.
    Setup BitTorrent Sync as you want it.
    In your preferences (Bittorrent Client) go to 
    Directories -> Location of .torrents -> Automatically load .torrents from -> YOUR SYNC DIRECTORY
    Also, check the delete loaded .torrents and enable search in subdirectoties.
    Now open Sinema and go to settings, in General -> Transfer Method choose BitTorrent Sync.
    Go back to the main page and you will be asked to choose your SYNC DIRECTORY. (Long hold the directory).
    If you have accidently chosen the wrong path,
    you can always go to settings -> general -> BitTorrent Sync Path and just
    remove the text in there. Once you go back to the main page, it will ask you to choose the path again.
    This will allow you to download from anywhere, anytime (as long as you have a connection to the internet).
    Once you start your computer, BitTorrent Sync will synchronise (no shit) and load the torrents into BitTorrent Client
    Just make sure BitTorrent Sync is running on the background of your smartphone when you start synchronising with your 
    computer.

## Code Example

    -

## Motivation

    -

## Installation

    -

## API Reference

    com.android.support:appcompat-v7:19.+
    jsoup-1.7.3.jar
    unbescape-1.1.0.jar
    universal-image-loader-1.9.2.jar
    yts.re API

## Tests

    -

## Contributors

    -
    
## Issues

    -

## Licence

    Copyright 2014 Nami Shah
    
       Licensed under the Apache License, Version 2.0 (the "License");
       you may not use this file except in compliance with the License.
       You may obtain a copy of the License at
    
           http://www.apache.org/licenses/LICENSE-2.0
    
       Unless required by applicable law or agreed to in writing, software
       distributed under the License is distributed on an "AS IS" BASIS,
       WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
       See the License for the specific language governing permissions and
       limitations under the License.

## Screenshots

![alt text](https://raw.githubusercontent.com/ShahNami/Sinema/master/screenshots/1.png?raw=true "Option screen")
![alt text](https://raw.githubusercontent.com/ShahNami/Sinema/master/screenshots/2.png?raw=true "Movie screen")
![alt text](https://raw.githubusercontent.com/ShahNami/Sinema/master/screenshots/3.png?raw=true "Settings")
![alt text](https://raw.githubusercontent.com/ShahNami/Sinema/master/screenshots/4.png?raw=true "TV Guide")
![alt text](https://raw.githubusercontent.com/ShahNami/Sinema/master/screenshots/5.png?raw=true "TV Guide")
![alt text](https://raw.githubusercontent.com/ShahNami/Sinema/master/screenshots/6.png?raw=true "TV Guide")
