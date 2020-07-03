package br.com.caelum.leilao.servico;
import br.com.caelum.leilao.builder.CriadorDeLeilao;
import br.com.caelum.leilao.dominio.Leilao;
import br.com.caelum.leilao.infra.dao.EnviadorDeEmail;
import br.com.caelum.leilao.infra.dao.LeilaoDao;
import br.com.caelum.leilao.infra.dao.RepositorioDeLeiloes;
import org.junit.*;
import org.mockito.InOrder;

import java.util.Arrays;
import java.util.Calendar;
import java.util.List;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

public class EncerradorDeLeilaoTest {

    private RepositorioDeLeiloes repositorioDeLeiloes;
    private EnviadorDeEmail enviadorDeEmail;

    @Before
    public void setUp() {
        this.repositorioDeLeiloes  = mock(RepositorioDeLeiloes.class);
        this.enviadorDeEmail = mock(EnviadorDeEmail.class);
    }

    @Test
    public void deveEncerrarLeiloesQueComecaramUmaSemanaAtras() {

        Calendar antiga = Calendar.getInstance();
        antiga.set(1999, 1, 20);

        Leilao leilao1 = new CriadorDeLeilao().para("TV de plasma")
                .naData(antiga).constroi();
        Leilao leilao2 = new CriadorDeLeilao().para("Geladeira")
                .naData(antiga).constroi();
        List<Leilao> leiloesAntigos = Arrays.asList(leilao1, leilao2);

        when(this.repositorioDeLeiloes.correntes()).thenReturn(leiloesAntigos);

        EncerradorDeLeilao encerrador = new EncerradorDeLeilao(this.repositorioDeLeiloes, this.enviadorDeEmail);
        encerrador.encerra();

        assertEquals(2, encerrador.getTotalEncerrados());
        assertTrue(leilao1.isEncerrado());
        assertTrue(leilao2.isEncerrado());
    }

    @Test
    public void naoDeveEncerrarLeiloesQueComecaramMenosDeUmaSemanaAtras() {
        Calendar antiga = Calendar.getInstance();
        antiga.set(Calendar.DAY_OF_MONTH, -1);

        Leilao leilao1 = new CriadorDeLeilao().para("TV").naData(antiga).constroi();
        Leilao leilao2 = new CriadorDeLeilao().para("Carro").naData(antiga).constroi();

        when(this.repositorioDeLeiloes.correntes()).thenReturn(Arrays.asList(leilao1, leilao2));

        EncerradorDeLeilao encerrador = new EncerradorDeLeilao(this.repositorioDeLeiloes, this.enviadorDeEmail);
        encerrador.encerra();

        assertEquals(0, encerrador.getTotalEncerrados());
        assertFalse(leilao1.isEncerrado());
        assertFalse(leilao2.isEncerrado());
    }

    @Test
    public void naoDeveEncerrarLeiloesCasoNaoHajaNenhum(){
        this.repositorioDeLeiloes = mock(RepositorioDeLeiloes.class);
        this.enviadorDeEmail = mock(EnviadorDeEmail.class);

        when(this.repositorioDeLeiloes.correntes()).thenReturn(Arrays.asList());

        EncerradorDeLeilao encerradorDeLeilao = new EncerradorDeLeilao(this.repositorioDeLeiloes, this.enviadorDeEmail);
        encerradorDeLeilao.encerra();

        assertEquals(0, encerradorDeLeilao.getTotalEncerrados());
    }

    @Test
    public void deveAtualizarLeiloesEncerrados() {

        Calendar antiga = Calendar.getInstance();
        antiga.set(1999, 1, 20);

        Leilao leilao1 = new CriadorDeLeilao().para("TV de plasma")
                .naData(antiga).constroi();

        when(this.repositorioDeLeiloes.correntes()).thenReturn(Arrays.asList(leilao1));

        EncerradorDeLeilao encerrador = new EncerradorDeLeilao(this.repositorioDeLeiloes, this.enviadorDeEmail);
        encerrador.encerra();

        // verificando que o metodo atualiza foi realmente invocado!
        verify(this.repositorioDeLeiloes).atualiza(leilao1);
        verify(this.repositorioDeLeiloes, times(1)).atualiza(leilao1);
    }

    @Test
    public void naoDeveEncerrarLeiloesQueComecaramMenosDeUmaSemanaAtrasNaoChamaAtualiza() {

        Calendar ontem = Calendar.getInstance();
        ontem.add(Calendar.DAY_OF_MONTH, -1);

        Leilao leilao1 = new CriadorDeLeilao().para("TV de plasma")
                .naData(ontem).constroi();
        Leilao leilao2 = new CriadorDeLeilao().para("Geladeira")
                .naData(ontem).constroi();


        verify(this.repositorioDeLeiloes, never()).atualiza(leilao1);
        verify(this.repositorioDeLeiloes, never()).atualiza(leilao2);
    }

    @Test
    public void deveEnviarEmailAposPersistirLeilaoEncerrado() {

        Calendar antiga = Calendar.getInstance();
        antiga.set(1999, 1, 20);

        Leilao leilao1 = new CriadorDeLeilao().para("TV de plasma")
                .naData(antiga).constroi();

        when(this.repositorioDeLeiloes.correntes()).thenReturn(Arrays.asList(leilao1));

        EncerradorDeLeilao encerradorDeLeilao = new EncerradorDeLeilao(this.repositorioDeLeiloes, enviadorDeEmail);
        encerradorDeLeilao.encerra();

        //Passa os mocks que serão verificados
        InOrder inOrder = inOrder(this.repositorioDeLeiloes, this.enviadorDeEmail);
        inOrder.verify(this.repositorioDeLeiloes, times(1)).atualiza(leilao1);
        inOrder.verify(this.enviadorDeEmail, times(1)).envia(leilao1);

    }

}
