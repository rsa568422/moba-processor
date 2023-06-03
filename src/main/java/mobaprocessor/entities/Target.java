package mobaprocessor.entities;

public enum Target {
    CLASSES ("classes"),
    ORIGINS ("origins");

    private final String postfix;

    Target(String postfix) {
        this.postfix = postfix;
    }

    @Override
    public String toString() {
        return postfix;
    }
}
