# EncryptDroid

An Encrypted Tool Based on AndroCrypt[1]

Usages:

1. Please configure autodroid.properties first.

2. Usage：java -jar EncryptDroid.jar [OriginalApkPath] [EncryptedApkPath] [AndroidId]
	
    * If [OriginalApkPath] is a directory， all apks in this directory will be encrypted. [EncryptedApkPath] must be a directory as well.
	
    * If [OriginalApkPath] is an apk file path，only this apk will be encrypted and saved to [EncryptedApkPath] file.
	
    * [AndroidId] means the id of Android platform version. Use command 'android list' to view your ids in your system.





[1]Kim D, Gokhale A, Ganapathy V, et al. Detecting plagiarized mobile apps using API birthmarks[J]. Automated Software Engineering, 2016, 23(4): 591-618.
