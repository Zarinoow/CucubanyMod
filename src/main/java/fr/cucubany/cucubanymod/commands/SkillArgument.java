package fr.cucubany.cucubanymod.commands;

import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.arguments.ArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.suggestion.Suggestions;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;
import fr.cucubany.cucubanymod.roleplay.education.PlayerSkill;
import fr.cucubany.cucubanymod.roleplay.education.Skill;
import net.minecraft.commands.SharedSuggestionProvider;

import java.util.Arrays;
import java.util.Collection;
import java.util.concurrent.CompletableFuture;

public class SkillArgument implements ArgumentType<String> {

    private static final Collection<String> EXAMPLES = Arrays.asList("outdoor_survival", "engineering", "medicine", "cooking", "building", "defense");
    @Override
    public String parse(StringReader reader) throws CommandSyntaxException {
        for(String skill : EXAMPLES) {
            if(reader.canRead(skill.length()) && skill.equalsIgnoreCase(reader.getString().substring(reader.getCursor(), reader.getCursor() + skill.length()))) {
                reader.setCursor(reader.getCursor() + skill.length());
                return skill;
            }
        }
        throw CommandSyntaxException.BUILT_IN_EXCEPTIONS.dispatcherUnknownArgument().createWithContext(reader);
    }

    public static SkillArgument skill() {
        return new SkillArgument();
    }

    @Override
    public Collection<String> getExamples() {
        return EXAMPLES;
    }

    @Override
    public <S> CompletableFuture<Suggestions> listSuggestions(CommandContext<S> context, SuggestionsBuilder builder) {
        return SharedSuggestionProvider.suggest(EXAMPLES.stream(), builder);
    }
}
