package net.microfalx.bootstrap.cli;

import net.microfalx.bootstrap.cli.util.Console;
import org.junit.jupiter.api.Test;

class ConsoleTest {

    private final Console console = Console.get();

    @Test
    void writePlain() {
        console.print("aaaa");
        console.printLn();
        console.printLn("bbbb");
    }

    @Test
    void writeOther() {
        console.printQuote().print("aaaa").printQuote();
    }

    @Test
    void writeColors() {
        console.printOk("aaaa");
        console.printFailure("bbbb");
        console.printBold("ccc");
    }

}