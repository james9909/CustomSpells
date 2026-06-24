package com.github.james9909.customspells;

import com.nisovin.magicspells.Subspell;
import com.nisovin.magicspells.castmodifiers.ModifierSet;
import com.nisovin.magicspells.util.config.ConfigData;

import java.util.List;

class DialogueOption {
    ConfigData<String> text;
    ConfigData<String> textSelected;

    List<String> modifiers;
    ModifierSet modifierSet;

    String spellOnClickName;
    String spellOnSelectName;
    String spellOnRightClickName;
    String spellOnLeftClickName;

    Subspell spellOnClick;
    Subspell spellOnSelect;
    Subspell spellOnRightClick;
    Subspell spellOnLeftClick;

    boolean hasAnyAction() {
        return spellOnClick != null || spellOnSelect != null || spellOnRightClick != null || spellOnLeftClick != null;
    }
}
