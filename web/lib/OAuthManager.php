<?php
include_once __DIR__ . '/../config.php';

class OAuthManager
{

    public $CurlHeaders;
    public $ResponseCode;

    private $_AuthorizeUrl;
    private $_AccessTokenUrl;
    private $_UserInfoUrl;
    private $_verifyPeer;
    private $_cafilePath;

    public function __construct()
    {
        $serverUrl = IS_URL;
        $this->_AuthorizeUrl  = IS_URL;
        $this->_LogoutUrl  = $serverUrl . "/auth/realms/" . IS_TENANT . "/protocol/openid-connect/logout";
        $this->_AccessTokenUrl  = $serverUrl . "/auth/realms/" . IS_TENANT . "/protocol/openid-connect/token";
        $this->_UserInfoUrl = $serverUrl . "/auth/realms/" . IS_TENANT . "/protocol/openid-connect/userinfo";
        $this->_verifyPeer = false;
        $this->CurlHeaders = array();
        $this->ResponseCode = 0;
    }

    // Convert an authorization code from callback into an access token.
    public function getAccessToken($client_id, $client_secret, $auth_code, $redirect_url)
    {
        // Init cUrl.
        $r = $this->initCurl($this->_AccessTokenUrl);

        // Add client ID and client secret to the headers.
        curl_setopt($r, CURLOPT_HTTPHEADER, array(
            "Authorization: Basic " . base64_encode($client_id . ":" . $client_secret),
        ));

        // Assemble POST parameters for the request.
        $post_fields = "code=" . urlencode($auth_code) . "&grant_type=authorization_code&redirect_uri=" . urlencode($redirect_url);

        // Obtain and return the access token from the response.
        curl_setopt($r, CURLOPT_POST, true);
        curl_setopt($r, CURLOPT_POSTFIELDS, $post_fields);

        $response = curl_exec($r);
        if ($response == false) {
            die("curl_exec() failed. Error: " . curl_error($r));
        }

        //Parse JSON return object.
        return json_decode($response);
    }


    public function getAccessTokenFromPasswordGrantType($client_key, $client_secret, $username, $password)
    {
        // Init cUrl.
        $r = $this->initCurl($this->_AccessTokenUrl);

        // Add client ID and client secret to the headers.
        curl_setopt($r, CURLOPT_HTTPHEADER, array(
            "Authorization: Basic " . base64_encode($client_key. ":" . $client_secret)
        ));

        // Assemble POST parameters for the request.
        $post_fields = "grant_type=password&username=" . $username . "&password=" . $password . "&scope=openid";

        // Obtain and return the access token from the response.
        curl_setopt($r, CURLOPT_POST, true);
        curl_setopt($r, CURLOPT_POSTFIELDS, $post_fields);

        $response = curl_exec($r);
        if ($response == false) {
            die("curl_exec() failed. Error: " . curl_error($r));
        }

        //Parse JSON return object.
        return json_decode($response);
    }

    // To get a refreshed access token
    public function getRefreshedAccessToken($client_key, $client_secret, $refresh_token)
    {
        // Init cUrl.
        $r = $this->initCurl($this->_AccessTokenUrl);

        // Add client ID and client secret to the headers.
        curl_setopt($r, CURLOPT_HTTPHEADER, array(
            "Authorization: Basic " . base64_encode($client_key . ":" . $client_secret),
        ));

        // Assemble POST parameters for the request.
        $post_fields = "refresh_token=" . urlencode($refresh_token) . "&grant_type=refresh_token";

        // Obtain and return the access token from the response.
        curl_setopt($r, CURLOPT_POST, true);
        curl_setopt($r, CURLOPT_POSTFIELDS, $post_fields);

        $response = curl_exec($r);
        if ($response == false) {
            die("curl_exec() failed. Error: " . curl_error($r));
        }

        //Parse JSON return object.
        return json_decode($response);
    }

    // Function to get OAuth logout url
    // refer http://xacmlinfo.org/2015/01/08/openid-connect-identity-server/ for OpenID Connect logout information
    public function getOAuthLogoutUrl($redirect_url, $applicationName)
    {
        return ($this->_LogoutUrl . "&commonAuthCallerPath=" . $redirect_url . "&relyingParty=" . $applicationName);
    }

    private function initCurl($url)
    {
        $r = null;

        if (($r = @curl_init($url)) == false) {
            header("HTTP/1.1 500", true, 500);
            die("Cannot initialize cUrl session. Is cUrl enabled for your PHP installation?");
        }

        curl_setopt($r, CURLOPT_RETURNTRANSFER, 1);

        // Decode compressed responses.
        curl_setopt($r, CURLOPT_ENCODING, 1);

        curl_setopt($r, CURLOPT_SSL_VERIFYPEER, $this->_verifyPeer);
        if($this->_verifyPeer){
            curl_setopt($r, CURLOPT_CAINFO, $this->_cafilePath);
        }

        return ($r);
    }


    public function getUserProfile($access_token)
    {
        $r = $this->initCurl($this->_UserInfoUrl);

        curl_setopt($r, CURLOPT_HTTPHEADER, array(
            "Authorization: Bearer " . $access_token
        ));

        $response = curl_exec($r);
        if ($response == false) {
            die("curl_exec() failed. Error: " . curl_error($r));
        }

        //Parse JSON return object.
        return json_decode($response);
    }

    // A generic function that executes an API request.
    public function execRequest($url, $access_token, $get_params)
    {
        // Create request string.
        $full_url = http_build_query($url, $get_params);

        $r = $this->initCurl($full_url);

        curl_setopt($r, CURLOPT_HTTPHEADER, array(
            "Authorization: Basic " . base64_encode($access_token)
        ));

        $response = curl_exec($r);
        if ($response == false) {
            die("curl_exec() failed. Error: " . curl_error($r));
        }

        //Parse JSON return object.
        return json_decode($response);
    }
}