package com.example.demo.cli.shell;

import org.jline.reader.History;
import org.jline.reader.LineReader;
import org.jline.reader.LineReaderBuilder;
import org.jline.reader.Parser;
import org.jline.terminal.Terminal;
import org.jline.terminal.TerminalBuilder;
import org.jline.utils.AttributedString;
import org.jline.utils.AttributedStyle;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Lazy;
import org.springframework.context.annotation.Primary;
import org.springframework.context.annotation.Profile;
import org.springframework.shell.jline.PromptProvider;

import java.io.IOException;

/**
 * Spring Shell için basitleştirilmiş konfigürasyon.
 * Sadece temel bileşenleri tanımlar.
 */
@Configuration
@Profile("cli")
public class ShellConfig {

    /**
     * Terminal bean'i oluşturur
     */
    @Bean
    @Primary
    @Lazy
    public Terminal customTerminal() throws IOException {
        return TerminalBuilder.builder()
                .system(true)
                .build();
    }
    
    /**
     * CLI için özelleştirilmiş prompt
     */
    @Bean
    public PromptProvider promptProvider() {
        return () -> new AttributedString("restoran-cli:> ", 
                AttributedStyle.DEFAULT.foreground(AttributedStyle.GREEN));
    }
}
