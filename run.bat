@echo off
title SmartBudget

echo.
echo === SmartBudget Launcher ===
echo.

:: Verification de Java
java -version >nul 2>&1
if %ERRORLEVEL% NEQ 0 (
    echo [ERREUR] Java n'est pas installe ou pas dans le PATH.
    echo Installez Java JDK 17+ depuis : https://adoptium.net/
    echo.
    pause
    exit /b 1
)
echo [OK] Java detecte.

:: Verification du driver JDBC
if not exist "lib\mysql-connector-j.jar" (
    echo.
    echo [ERREUR] Driver MySQL introuvable : lib\mysql-connector-j.jar
    echo Telechargez MySQL Connector/J depuis :
    echo https://dev.mysql.com/downloads/connector/j/
    echo Placez le .jar dans lib\ et renommez-le mysql-connector-j.jar
    echo.
    pause
    exit /b 1
)
echo [OK] Driver JDBC detecte.

:: Verification de la configuration
if not exist "config\database.properties" (
    copy "config\database.properties.example" "config\database.properties" >nul 2>&1
    echo [OK] config\database.properties cree depuis l'exemple.
)
echo [OK] Configuration presente.

:: Nettoyage
if exist bin rmdir /s /q bin
mkdir bin

:: Compilation
echo.
echo Compilation en cours...

javac -encoding UTF-8 -cp "lib\mysql-connector-j.jar" -d bin src\model\User.java src\model\Category.java src\model\Transaction.java src\model\Budget.java src\model\SavingsGoal.java src\utils\PasswordUtils.java src\utils\ValidationUtils.java src\utils\FormatUtils.java src\utils\SessionManager.java src\database\DatabaseConnection.java src\database\DatabaseInitializer.java src\database\UserDAO.java src\database\CategoryDAO.java src\database\TransactionDAO.java src\database\BudgetDAO.java src\database\SavingsGoalDAO.java src\controller\SessionManager.java src\controller\LoginController.java src\controller\TransactionController.java src\controller\BudgetController.java src\controller\SavingsController.java src\view\AppColors.java src\view\UIComponents.java src\view\DatePickerField.java src\view\LoginView.java src\view\MainView.java src\view\DashboardView.java src\view\TransactionsView.java src\view\BudgetView.java src\view\SavingsView.java src\view\StatisticsView.java src\Main.java

if %ERRORLEVEL% NEQ 0 (
    echo.
    echo [ERREUR] La compilation a echoue.
    echo Verifiez que vous utilisez Java JDK 17 ou superieur.
    echo.
    pause
    exit /b 1
)
echo [OK] Compilation reussie.

:: Lancement
echo.
echo Lancement de SmartBudget...
echo.

java -cp "bin;lib\mysql-connector-j.jar" -Dfile.encoding=UTF-8 Main

if %ERRORLEVEL% NEQ 0 (
    echo.
    echo [ERREUR] L'application s'est arretee avec une erreur.
    echo Consultez les messages ci-dessus.
    echo.
    pause
)
