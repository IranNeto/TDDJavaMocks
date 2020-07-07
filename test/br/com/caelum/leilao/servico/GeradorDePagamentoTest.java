package br.com.caelum.leilao.servico;

import br.com.caelum.leilao.builder.CriadorDeLeilao;
import br.com.caelum.leilao.dominio.Leilao;
import br.com.caelum.leilao.dominio.Pagamento;
import br.com.caelum.leilao.dominio.Usuario;
import br.com.caelum.leilao.infra.dao.Relogio;
import br.com.caelum.leilao.infra.dao.RepositorioDeLeiloes;
import br.com.caelum.leilao.infra.dao.RepositorioDePagamentos;
import org.junit.Test;
import org.mockito.ArgumentCaptor;

import java.util.Arrays;
import java.util.Calendar;

import static junit.framework.TestCase.assertEquals;
import static org.mockito.Mockito.*;

public class GeradorDePagamentoTest {

    @Test
    public void deveGerarPagamentoParaUmLeilaoEncerrado() {

        RepositorioDePagamentos repositorioDePagamentos = mock(RepositorioDePagamentos.class);
        RepositorioDeLeiloes repositorioDeLeiloes = mock(RepositorioDeLeiloes.class);
        Avaliador avaliador = new Avaliador();

        Leilao leilao = new CriadorDeLeilao().para("Produto 1").lance(new Usuario("User A"), 2000.00).lance(new Usuario("User B"), 2500.0).constroi();
        when(repositorioDeLeiloes.encerrados()).thenReturn(Arrays.asList(leilao));

        GeradorDePagamento geradorDePagamento = new GeradorDePagamento(repositorioDeLeiloes, repositorioDePagamentos, avaliador);
        geradorDePagamento.gera();

        //Validar o pagamento e o seu valor
        ArgumentCaptor<Pagamento> argumentCaptor = ArgumentCaptor.forClass(Pagamento.class);
        verify(repositorioDePagamentos).salva(argumentCaptor.capture());
        Pagamento pagamentoCapturado = argumentCaptor.getValue();

        assertEquals(2500.0, pagamentoCapturado.getValor());

    }

    @Test
    public void deveGerarPagamentoProximoDiaUtil(){
        RepositorioDeLeiloes leiloes = mock(RepositorioDeLeiloes.class);
        RepositorioDePagamentos pagamentos = mock(RepositorioDePagamentos.class);
        Relogio relogio = mock(Relogio.class);

        Calendar sabado = Calendar.getInstance();
        sabado.set(2012, Calendar.APRIL, 7);

        when(relogio.hoje()).thenReturn(sabado);

        Leilao leilao = new CriadorDeLeilao()
                .para("Playstation")
                .lance(new Usuario("José da Silva"), 2000.0)
                .lance(new Usuario("Maria Pereira"), 2500.0)
                .constroi();

        when(leiloes.encerrados()).thenReturn(Arrays.asList(leilao));

        GeradorDePagamento gerador =
                new GeradorDePagamento(leiloes, pagamentos, new Avaliador(), relogio);
        gerador.gera();

        ArgumentCaptor<Pagamento> argumento = ArgumentCaptor.forClass(Pagamento.class);
        verify(pagamentos).salva(argumento.capture());
        Pagamento pagamentoGerado = argumento.getValue();

        assertEquals(Calendar.MONDAY, pagamentoGerado.getData().get(Calendar.DAY_OF_WEEK));
        assertEquals(9, pagamentoGerado.getData().get(Calendar.DAY_OF_MONTH));
    }

    @Test
    public void deveGerarPagamentoNoDiaUtil(){
        RepositorioDeLeiloes leiloes = mock(RepositorioDeLeiloes.class);
        RepositorioDePagamentos pagamentos = mock(RepositorioDePagamentos.class);
        Relogio relogio = mock(Relogio.class);

        Calendar sexta = Calendar.getInstance();
        sexta.set(2012, Calendar.APRIL, 6);

        when(relogio.hoje()).thenReturn(sexta);

        Leilao leilao = new CriadorDeLeilao()
                .para("Playstation")
                .lance(new Usuario("José da Silva"), 2000.0)
                .lance(new Usuario("Maria Pereira"), 2500.0)
                .constroi();

        when(leiloes.encerrados()).thenReturn(Arrays.asList(leilao));

        GeradorDePagamento gerador =
                new GeradorDePagamento(leiloes, pagamentos, new Avaliador(), relogio);
        gerador.gera();

        ArgumentCaptor<Pagamento> argumento = ArgumentCaptor.forClass(Pagamento.class);
        verify(pagamentos).salva(argumento.capture());
        Pagamento pagamentoGerado = argumento.getValue();

        assertEquals(Calendar.FRIDAY, pagamentoGerado.getData().get(Calendar.DAY_OF_WEEK));
        assertEquals(6, pagamentoGerado.getData().get(Calendar.DAY_OF_MONTH));
    }

}
