package selenium;

import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;

public class TestesSelenium {

    // ============================================================
    // CONFIGURAR ANTES DE RODAR:
    //   1. Baixe o ChromeDriver compativel com sua versao do Opera GX:
    //      https://googlechromelabs.github.io/chrome-for-testing/
    //      (Opera GX usa Chromium — versao esta em Opera GX > Sobre)
    //   2. Ajuste os dois caminhos abaixo
    //   3. Confirme usuario/senha do admin no banco
    // ============================================================
    static final String CHROMEDRIVER_PATH = "C:\\Users\\DELL\\Downloads\\chromedriver.exe";
    static final String OPERA_GX_PATH     = "C:\\Program Files\\Google\\Chrome\\Application\\chrome.exe";
    static final String BASE_URL          = "http://localhost:5173";
    static final String ADMIN_USER        = "admin";
    static final String ADMIN_PASS        = "admin123";

    public static void main(String[] args) throws InterruptedException {
        System.setProperty("webdriver.chrome.driver", CHROMEDRIVER_PATH);

        ChromeOptions options = new ChromeOptions();
        options.setBinary(OPERA_GX_PATH);

        WebDriver driver = new ChromeDriver(options);
        driver.manage().window().maximize();

        try {
            testeNavegacao(driver);
            testeAcessoProtegido(driver);
            testeLoginInvalido(driver);
            testeLoginValido(driver);
        } finally {
            driver.quit();
        }

        System.out.println("\n=== Todos os testes concluidos ===");
    }

    // ------------------------------------------------------------------
    // Teste 1: Paginas publicas carregam corretamente
    // ------------------------------------------------------------------
    static void testeNavegacao(WebDriver driver) throws InterruptedException {
        System.out.println("\n--- Teste 1: Navegacao basica ---");

        driver.get(BASE_URL + "/");
        Thread.sleep(1000);
        if (driver.getCurrentUrl().contains("localhost:5173")) {
            System.out.println("Pagina inicial carregou com sucesso!");
        } else {
            System.out.println("Falha ao carregar pagina inicial.");
        }

        driver.get(BASE_URL + "/sobre");
        Thread.sleep(1000);
        if (driver.getCurrentUrl().contains("sobre")) {
            System.out.println("Pagina Sobre carregou com sucesso!");
        } else {
            System.out.println("Falha ao carregar pagina Sobre.");
        }

        driver.get(BASE_URL + "/servicos");
        Thread.sleep(1000);
        if (driver.getCurrentUrl().contains("servicos")) {
            System.out.println("Pagina Servicos carregou com sucesso!");
        } else {
            System.out.println("Falha ao carregar pagina Servicos.");
        }
    }

    // ------------------------------------------------------------------
    // Teste 2: Acesso a rota protegida sem login redireciona para /login
    // ------------------------------------------------------------------
    static void testeAcessoProtegido(WebDriver driver) throws InterruptedException {
        System.out.println("\n--- Teste 2: Acesso protegido sem login ---");

        driver.get(BASE_URL + "/admin");
        Thread.sleep(1500);

        if (driver.getCurrentUrl().contains("login")) {
            System.out.println("Redirecionamento para login funcionou!");
        } else {
            System.out.println("Falha: conseguiu acessar /admin sem login.");
        }
    }

    // ------------------------------------------------------------------
    // Teste 3: Login com credenciais invalidas nao autentica
    // ------------------------------------------------------------------
    static void testeLoginInvalido(WebDriver driver) throws InterruptedException {
        System.out.println("\n--- Teste 3: Login invalido ---");

        driver.get(BASE_URL + "/login");
        Thread.sleep(1000);

        driver.findElement(By.id("username")).sendKeys("usuario_inexistente");
        driver.findElement(By.id("password")).sendKeys("senha_errada");
        driver.findElement(By.cssSelector("button[type='submit']")).click();

        Thread.sleep(2000);

        if (driver.getCurrentUrl().contains("login")) {
            System.out.println("Login invalido bloqueado com sucesso!");
        } else {
            System.out.println("Falha: login invalido foi aceito.");
        }
    }

    // ------------------------------------------------------------------
    // Teste 4: Login com credenciais validas redireciona para painel
    // ------------------------------------------------------------------
    static void testeLoginValido(WebDriver driver) throws InterruptedException {
        System.out.println("\n--- Teste 4: Login valido (admin) ---");

        driver.get(BASE_URL + "/login");
        Thread.sleep(1000);

        driver.findElement(By.id("username")).clear();
        driver.findElement(By.id("username")).sendKeys(ADMIN_USER);
        driver.findElement(By.id("password")).clear();
        driver.findElement(By.id("password")).sendKeys(ADMIN_PASS);
        driver.findElement(By.cssSelector("button[type='submit']")).click();

        Thread.sleep(2500);

        if (driver.getCurrentUrl().contains("admin") || driver.getCurrentUrl().contains("cliente")) {
            System.out.println("Login realizado com sucesso!");
        } else {
            System.out.println("Falha no login.");
        }
    }

}
