<?php
session_start();

if (isset($_SESSION['username'])) {
    if(!isset($_GET['code']) && isset($_GET['id'])){
        $key = "4ec77a6a3e1f6495690057fbca630441bca9a71b";
        $authorizeUrl = "https://figshare.com/account/applications/authorize";
        $redirectUrl = "https://localhost/web/figshare.php";
        $url = $authorizeUrl . "?client_id=" . $key . "&response_type=code&scope=all&state=" . urlencode($_GET['id'])
            . "&redirect_uri=" . $redirectUrl;
        header('Location: ' . $url);
    }else if(isset($_GET['code']) && isset($_GET['state'])){
        var_dump($_GET['code']);
    }else{
        $home_url = 'https://' . $_SERVER['HTTP_HOST'] . dirname($_SERVER['PHP_SELF']) . '/index.php';
        header('Location: ' . $home_url);
    }
} else {
    $home_url = 'https://' . $_SERVER['HTTP_HOST'] . dirname($_SERVER['PHP_SELF']) . '/index.php';
    header('Location: ' . $home_url);
}