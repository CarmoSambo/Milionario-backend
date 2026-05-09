package Game.Milionario.Configuration;

// CORRIGIDO: removida a anotação @Component para que o Spring não instancie
// este bean vazio. Quando a autenticação for implementada, este filtro
// deve estender OncePerRequestFilter e implementar doFilterInternal().
public class SecurityFilter {
}
