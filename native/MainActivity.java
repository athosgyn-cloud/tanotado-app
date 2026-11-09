package br.app.tanotado;

import android.os.Bundle;
import android.webkit.WebView;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import com.getcapacitor.BridgeActivity;
import com.getcapacitor.WebViewListener;

/**
 * No Android 15+ o app desenha por baixo da barra de status e da barra de navegação.
 * Aqui medimos essas barras (e o teclado) e passamos para a página em dp,
 * para o layout deixar o espaço certo em cima e embaixo.
 */
public class MainActivity extends BridgeActivity {

    private int top = 0, bottom = 0, ime = 0;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        final WebView webView = getBridge().getWebView();
        final float density = getResources().getDisplayMetrics().density;

        ViewCompat.setOnApplyWindowInsetsListener(webView, (v, insets) -> {
            Insets bars = insets.getInsets(WindowInsetsCompat.Type.systemBars() | WindowInsetsCompat.Type.displayCutout());
            Insets kb = insets.getInsets(WindowInsetsCompat.Type.ime());
            top = Math.round(bars.top / density);
            bottom = Math.round(bars.bottom / density);
            ime = Math.round(kb.bottom / density);
            push(webView);
            return ViewCompat.onApplyWindowInsets(v, insets);
        });

        getBridge().addWebViewListener(new WebViewListener() {
            @Override
            public void onPageLoaded(WebView view) {
                push(view);
            }
        });
        ViewCompat.requestApplyInsets(webView);
    }

    private void push(WebView webView) {
        final String js = "(function(){var a=[" + top + "," + bottom + "," + ime + "];"
            + "if(window.__insets){window.__insets(a[0],a[1],a[2])}else{window.__insPending=a}})()";
        webView.post(() -> webView.evaluateJavascript(js, null));
    }
}
