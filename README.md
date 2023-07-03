# JTube Lite
Light version of JTube

## Building

```
You will need:
S40 5th Edition SDK
JDK 1.5.0 (to install S40 SDK)
Last version of Eclipse SDK
MTJ plugin 2.0.1
```

Clone the repository<br>

Import project from directory in Eclipse SDK<br>

Open "Application Descriptor" in Package Explorer
![image](https://user-images.githubusercontent.com/43963888/154848600-b6f30e9c-a412-4771-80bf-527afe11076e.png)<br>

Click on "Create package"
![image](https://user-images.githubusercontent.com/43963888/154848614-72752480-b988-40cd-a3c6-9cad1e02d77c.png)<br>

Check the "Use deployment directory"<br>

Uncheck "Obfuscate the code" if you don't want to optimize code<br>

Then press "Finish"<br>

![image](https://user-images.githubusercontent.com/43963888/154848648-2f054800-b72e-49e6-8b6c-7e3cb6d3c216.png)<br>

Builded JAR & JAD files will appear at \<project path\>/deployed/S40_5th_Edition_SDK/

# Setting up your own server

invproxy.php:
```
<?php
<?php
ini_set('error_reporting', E_ALL);
ini_set('display_errors', 1);
ini_set('display_startup_errors', 1);
$url = $_GET['u'];
if(strpos($url, 'file:') === 0) die();
$in = null;
$post = false;
$arr = getallheaders();
if(isset($_GET['post'])) {
	$post = true;
	$in = file_get_contents('php://input');
}
$h = array();
array_push($h, 'X-Forwarded-For: ' . $_SERVER['REMOTE_ADDR']);
foreach($arr as $k=>$v) {
	$lk = strtolower($k);
	if($lk == 'host' || $lk == 'connection' || $lk == 'accept-encoding' || $lk == 'accept-encoding')
		continue;
	array_push($h, $k . ': ' . $v);
}
$ch = curl_init();
curl_setopt($ch, CURLOPT_URL, $url);
curl_setopt($ch, CURLOPT_HTTPHEADER, $h);
curl_setopt($ch, CURLOPT_FOLLOWLOCATION, true);
curl_setopt($ch, CURLOPT_RETURNTRANSFER, 1);
curl_setopt($ch, CURLOPT_HEADER, true);
if($post) {
	curl_setopt($ch, CURLOPT_POST, 1);
	curl_setopt($ch, CURLOPT_POSTFIELDS, $in);
}
$res = curl_exec($ch);
$headerSize = curl_getinfo($ch, CURLINFO_HEADER_SIZE);
$h2 = substr($res, 0, $headerSize);
$body = substr($res, $headerSize);
$headersTmpArray = explode("\r\n", $h2);
for ($i = 0; $i < count($headersTmpArray); ++$i) {
	$s = $headersTmpArray[$i];
	if(strlen($s) > 0) {
		if(strpos($s, ':')) {
			$k = substr($s, 0, strpos($s, ':'));
			$v = substr($s, strpos($s, ':') + 1);
			$lk = strtolower($k);
			if($lk == 'server' || $lk == 'connection' || $lk == 'transfer-encoding')
				continue;
			header($s);
		}
	}
}
curl_close($ch);
echo $body;
?>

?>
```
