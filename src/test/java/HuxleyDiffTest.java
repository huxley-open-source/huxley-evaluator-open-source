import com.thehuxley.evaluator.diff.Diff;
import com.thehuxley.evaluator.diff.DiffLine;
import com.thehuxley.evaluator.diff.HuxleyDiff;
import org.junit.Before;
import org.junit.Test;

import java.util.List;

import static org.junit.Assert.*;

/**
 * Created by rodrigo on 15/10/15.
 */

public class HuxleyDiffTest {
    String dir = "/home/rodrigo/dev/The-Huxley-Evaluator/src/test/resources/diff/";

    HuxleyDiff huxleyDiff;
    Diff d;

    @Before
    public void setUp(){
        huxleyDiff = new HuxleyDiff();
    }

    @Test
    public void testSimple() {
        d = huxleyDiff.match(dir + "simple_line.expected", dir + "simple_line.actual", 10, 3, false);
        assertEquals(false, d.isMatch());
        assertEquals(1, d.getLines().size());
        assertEquals(1, d.getTotalLines());
        assertEquals("abc", d.getLines().get(0).getExpected());
        assertEquals("deb", d.getLines().get(0).getActual());

    }

    @Test
    public void testMatch() {
        d = huxleyDiff.match(dir + "simple_line_match.expected", dir + "simple_line_match.actual", 10, 3, false);
        assertEquals(true, d.isMatch());
        assertEquals(0, d.getLines().size());
        assertEquals(1, d.getTotalLines());
    }

    @Test
    public void testExpectedGreater() {
        // Testa o esperado com mais linhas
        d = huxleyDiff.match(dir+"complex.expected", dir+"simple_line.actual", 10,3 , false);

        assertEquals(false, d.isMatch());
        assertEquals(89, d.getTotalLines());
        assertEquals(10, d.getLines().size());
        assertEquals("deb", d.getLines().get(0).getActual());

        List<DiffLine> lines = d.getLines();
        for (int i = 0; i < 10; i++) {
            assertEquals(false, lines.get(i).isMatch());
            if (i>0) {
                assertEquals("", lines.get(i).getActual());
            }
        }
    }

    @Test
    public void testActualGreater() {
        // testa o obtido com mais linhas
        d = huxleyDiff.match(dir+"simple_line.actual", dir+"complex.expected", 10,3 , false);
        assertEquals(false, d.isMatch());
        assertEquals(89, d.getTotalLines());
        assertEquals(10, d.getLines().size());
        assertEquals("deb", d.getLines().get(0).getExpected());

        List<DiffLine> lines = d.getLines();
        for (int i = 0; i < 10; i++) {
            assertEquals(false, lines.get(i).isMatch());
            if (i>0) {
                assertEquals("", lines.get(i).getExpected());
            }
        }
    }

    @Test
    public void testDiffInBeginning() {
        // Diferença na linha 2, a linha 1 tem que aparecer como match. Deve mostrar também as linhas 3, 4 e 5
        d = huxleyDiff.match(dir+"complex.expected", dir+"complex1.txt", 10,3 , false);
        List<DiffLine> lines = d.getLines();
        assertEquals(true, lines.get(0).isMatch());
        assertEquals(false, lines.get(1).isMatch());
        assertEquals(3, lines.get(2).getNumber());
        assertEquals(4, lines.get(3).getNumber());
        assertEquals(5, lines.get(4).getNumber());

        // Como não há mais nenhuma diferença, deve se limitar a mostrar 3 de contexto
        assertEquals(5, lines.size());

    }

    @Test
    public void testShowInTheMiddle() {
        /*
        Deve mostrar as linhas 3, 4 e 5 como Match
        A linha 6 como diff, as linhas 7, 8 e 9 como true
         */
        d = huxleyDiff.match(dir+"complex.expected", dir+"complex2.txt", 10,3 , false);
        List<DiffLine> lines = d.getLines();
        assertEquals(true, lines.get(0).isMatch()); // linha 3
        assertEquals(true, lines.get(1).isMatch()); // linha 4
        assertEquals(true, lines.get(2).isMatch()); // linha 5
        assertEquals(false, lines.get(3).isMatch()); // linha 6
        assertEquals(true, lines.get(4).isMatch()); // linha 7
        assertEquals(true, lines.get(5).isMatch()); // linha 8
        assertEquals(true, lines.get(6).isMatch()); // linha 9

        assertEquals(7, lines.size());;
    }


    @Test
    public void testReachMax() {
        /*
        Deve mostrar as linhas 3, 4 e 5 como Match
        A linha 6 como diff, as linhas 7, 8 como true
        9 false
        10 true
        11 false
        12 true
        não deve mostrar a 13, pq alcançou o tamanho máximo
         */
        d = huxleyDiff.match(dir+"complex.expected", dir+"complex3.txt", 10,3 , false);
        List<DiffLine> lines = d.getLines();
        assertEquals(true, lines.get(0).isMatch()); // linha 3
        assertEquals(true, lines.get(1).isMatch()); // linha 4
        assertEquals(true, lines.get(2).isMatch()); // linha 5

        assertEquals(false, lines.get(3).isMatch()); // linha 6

        assertEquals(true, lines.get(4).isMatch()); // linha 7
        assertEquals(true, lines.get(5).isMatch()); // linha 8

        assertEquals(false, lines.get(6).isMatch()); // linha 9

        assertEquals(true, lines.get(7).isMatch()); // linha 10

        assertEquals(false, lines.get(8).isMatch()); // linha 11

        assertEquals(true, lines.get(9).isMatch()); // linha 12

        assertEquals(10, lines.size());
    }

    @Test
    public void testError() {
        d = huxleyDiff.match(dir+" não existe", dir+"complex3.txt", 10,3 , false);
        assertTrue(d.getHasError());
        assertNotEquals("",d.getStackTrace());

    }

//    @Test
//    public void testHugeFiles() {
//        d = huxleyDiff.match(dir+"big1.txt", dir+"big2.txt", 100,3 , false);
//        assertFalse(d.isMatch());
//        assertEquals(3276801, d.getTotalLines());
//    }

    @Test
    public void test3Lines() {
        d = huxleyDiff.match(dir+"3linhas.txt", dir+"sejabem_vindo.txt", 70,3 , false);
        assertFalse(d.getHasError());
    }



}
