<?php
    include 'config.php';
    include './lib/OAuthManager.php';

    if(isset($_POST['username']) && isset($_POST['password'])){
        $username = $_POST['username'];
        $password = $_POST['password'];

        //Todo verify login
        $fullUsername = $username;
        $oauthManager = new OAuthManager();
        try{
            $token = $oauthManager->getAccessTokenFromPasswordGrantType( IS_OAUTH_CLIENT_ID, IS_OAUTH_CLIENT_SECRET,
                $fullUsername, $password);
            if($token != null && isset($token->access_token)){
                session_start();
                $_SESSION['username'] = strtolower($username);
                $home_url = 'http://' . $_SERVER['HTTP_HOST'] . dirname($_SERVER['PHP_SELF']) . '/index.php';
                header('Location: ' . $home_url);
            }else{
                $loginFailed = true;
            }
        }catch (Exception $ex){
            $loginFailed = true;
        }
    }elseif(isset($_GET['code']) && !empty($_GET['code'])){
            $oauthManager = new OAuthManager();
            $response = $oauthManager->getAccessToken(IS_OAUTH_CLIENT_ID, IS_OAUTH_CLIENT_SECRET, $_GET['code'], IS_REDIRECT_URI);
            if(isset($response->access_token)){
              $profile = $oauthManager->getUserProfile($response->access_token);
              if(isset($profile->preferred_username)){
                  session_start();
                  $_SESSION['username'] = $profile->preferred_username;
                  $home_url = 'http://' . $_SERVER['HTTP_HOST'] . dirname($_SERVER['PHP_SELF']) . '/index.php';
                  header('Location: ' . $home_url);
              }
            }
            $loginFailed = true;
    }
?>

<html>
    <head>
        <!-- Latest compiled and minified CSS -->
        <link rel="stylesheet" href="https://maxcdn.bootstrapcdn.com/bootstrap/3.3.6/css/bootstrap.min.css"
              integrity="sha384-1q8mTJOASx8j1Au+a5WDVnPi2lkFfwwEAa8hDDdjZlpLegxhjVME1fgjWPGmkzs7" crossorigin="anonymous">

        <!-- Optional theme -->
        <link rel="stylesheet" href="assets/css/login.css">

    </head>
    <body>

        <nav class="navbar navbar-inverse navbar-fixed-top">
            <div class="container">
                <div class="navbar-header">
                    <button type="button" class="navbar-toggle collapsed" data-toggle="collapse" data-target="#navbar"
                            aria-expanded="false" aria-controls="navbar">
                        <span class="sr-only">Toggle navigation</span>
                        <span class="icon-bar"></span>
                        <span class="icon-bar"></span>
                        <span class="icon-bar"></span>
                    </button>
                    <a class="navbar-brand" href="./index.php">SEAGrid Data Catalog</a>
                </div>
                <div id="navbar" class="collapse navbar-collapse">
                    <ul class="nav navbar-nav">
                        <li><a href="./search.php">Search</a></li>
                        <li><a href="./browse.php">Directory Browser</a></li>
                    </ul>
                    <ul class="nav navbar-nav pull-right">
                        <li class="active"><a href="./login.php">Login</a></li>
                    </ul>
                </div>
            </div>
        </nav>

        <div class="container">
            <form class="form-signin" action="./login.php" method="post">
                <?php if(isset($loginFailed) && $loginFailed): ?>
                    <br>
                    <div class="alert alert-warning">
                        <strong>Failed!</strong> Provided username and password does not match.
                    </div>
                <?php endif; ?>
                <h2 class="form-signin-heading">Please sign in</h2>
                <label for="username" class="sr-only">Username</label>
                <input type="username" id="username" name="username" class="form-control" placeholder="Username" required autofocus>
                <br>
                <label for="inputPassword" class="sr-only">Password</label>
                <input type="password" id="password" name="password" class="form-control" placeholder="Password" required>
                <div class="checkbox">
                    <label>
                        <input type="checkbox" value="remember-me"> Remember me
                    </label>
                </div>
                <button class="btn btn-lg btn-primary btn-block" type="submit">Sign in</button>
                <a href="https://iam.scigap.org/auth/realms/seagrid/protocol/openid-connect/auth?response_type=code&amp;client_id=pga&amp;redirect_uri=https%3A%2F%2Fdata.seagrid.org%2Flogin.php&amp;scope=openid&amp;kc_idp_hint=cilogon" class="btn btn-primary btn-block btn-external-login">
                    Sign in with CILogon
                </a>
            </form>

        </div><!-- /.container -->

        <!--JQuery MinJS-->
        <script src="https://ajax.googleapis.com/ajax/libs/jquery/1.11.3/jquery.min.js"></script>
        <!-- Latest compiled and minified JavaScript -->
        <script src="https://maxcdn.bootstrapcdn.com/bootstrap/3.3.6/js/bootstrap.min.js"
                integrity="sha384-0mSbJDEHialfmuBBQP6A4Qrprq5OVfW37PRR3j5ELqxss1yVqOtnepnHVP9aJ7xS" crossorigin="anonymous"></script>
    </body>
</html>
