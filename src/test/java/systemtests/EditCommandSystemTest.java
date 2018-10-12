package systemtests;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertTrue;
import static seedu.learnvocabulary.logic.commands.CommandTestUtil.ADDRESS_DESC_AMY;
import static seedu.learnvocabulary.logic.commands.CommandTestUtil.ADDRESS_DESC_BOB;
import static seedu.learnvocabulary.logic.commands.CommandTestUtil.INVALID_ADDRESS_DESC;
import static seedu.learnvocabulary.logic.commands.CommandTestUtil.INVALID_NAME_DESC;
import static seedu.learnvocabulary.logic.commands.CommandTestUtil.INVALID_PHONE_DESC;
import static seedu.learnvocabulary.logic.commands.CommandTestUtil.INVALID_TAG_DESC;
import static seedu.learnvocabulary.logic.commands.CommandTestUtil.MEANING_DESC;
import static seedu.learnvocabulary.logic.commands.CommandTestUtil.NAME_DESC_AMY;
import static seedu.learnvocabulary.logic.commands.CommandTestUtil.NAME_DESC_BOB;
import static seedu.learnvocabulary.logic.commands.CommandTestUtil.PHONE_DESC_AMY;
import static seedu.learnvocabulary.logic.commands.CommandTestUtil.PHONE_DESC_BOB;
import static seedu.learnvocabulary.logic.commands.CommandTestUtil.TAG_DESC_FRIEND;
import static seedu.learnvocabulary.logic.commands.CommandTestUtil.TAG_DESC_HUSBAND;
import static seedu.learnvocabulary.logic.commands.CommandTestUtil.VALID_NAME_AMY;
import static seedu.learnvocabulary.logic.commands.CommandTestUtil.VALID_NAME_BOB;
import static seedu.learnvocabulary.logic.commands.CommandTestUtil.VALID_PHONE_AMY;
import static seedu.learnvocabulary.logic.commands.CommandTestUtil.VALID_TAG_HUSBAND;
import static seedu.learnvocabulary.logic.parser.CliSyntax.PREFIX_TAG;
import static seedu.learnvocabulary.model.Model.PREDICATE_SHOW_ALL_WORDS;
import static seedu.learnvocabulary.testutil.TypicalIndexes.INDEX_FIRST_WORD;
import static seedu.learnvocabulary.testutil.TypicalIndexes.INDEX_SECOND_WORD;
import static seedu.learnvocabulary.testutil.TypicalWords.AMY;
import static seedu.learnvocabulary.testutil.TypicalWords.BOB;
import static seedu.learnvocabulary.testutil.TypicalWords.KEYWORD_MATCHING_MEIER;

import org.junit.Test;

import seedu.learnvocabulary.commons.core.Messages;
import seedu.learnvocabulary.commons.core.index.Index;
import seedu.learnvocabulary.logic.commands.EditCommand;
import seedu.learnvocabulary.logic.commands.RedoCommand;
import seedu.learnvocabulary.logic.commands.UndoCommand;
import seedu.learnvocabulary.model.Model;
import seedu.learnvocabulary.model.tag.Tag;
import seedu.learnvocabulary.model.word.Address;
import seedu.learnvocabulary.model.word.Name;
import seedu.learnvocabulary.model.word.Phone;
import seedu.learnvocabulary.model.word.Word;
import seedu.learnvocabulary.testutil.WordBuilder;
import seedu.learnvocabulary.testutil.WordUtil;

public class EditCommandSystemTest extends LearnVocabularySystemTest {

    @Test
    public void edit() {
        Model model = getModel();

        /* ----------------- Performing edit operation while an unfiltered list is being shown ---------------------- */

        /* Case: edit all fields, command with leading spaces, trailing spaces and multiple spaces between each field
         * -> edited
         */
        Index index = INDEX_FIRST_WORD;
        String command = " " + EditCommand.COMMAND_WORD + "  " + index.getOneBased() + "  " + NAME_DESC_BOB + "  "
                + MEANING_DESC + " " + PHONE_DESC_BOB + " " + ADDRESS_DESC_BOB + " " + TAG_DESC_HUSBAND + " ";
        Word editedWord = new WordBuilder(BOB).withTags(VALID_TAG_HUSBAND).build();
        assertCommandSuccess(command, index, editedWord);

        /* Case: undo editing the last word in the list -> last word restored */
        command = UndoCommand.COMMAND_WORD;
        String expectedResultMessage = UndoCommand.MESSAGE_SUCCESS;
        assertCommandSuccess(command, model, expectedResultMessage);

        /* Case: redo editing the last word in the list -> last word edited again */
        command = RedoCommand.COMMAND_WORD;
        expectedResultMessage = RedoCommand.MESSAGE_SUCCESS;
        model.updateWord(
                getModel().getFilteredWordList().get(INDEX_FIRST_WORD.getZeroBased()), editedWord);
        assertCommandSuccess(command, model, expectedResultMessage);

        /* Case: edit a word with new values same as existing values -> edited */
        command = EditCommand.COMMAND_WORD + " " + index.getOneBased() + NAME_DESC_BOB + MEANING_DESC + PHONE_DESC_BOB
                + ADDRESS_DESC_BOB + TAG_DESC_FRIEND + TAG_DESC_HUSBAND;
        assertCommandSuccess(command, index, BOB);

        /* Case: edit a word with new values same as another word's values but with different name -> edited */
        assertTrue(getModel().getLearnVocabulary().getWordList().contains(BOB));
        index = INDEX_SECOND_WORD;
        assertNotEquals(getModel().getFilteredWordList().get(index.getZeroBased()), BOB);
        command = EditCommand.COMMAND_WORD + " " + index.getOneBased() + NAME_DESC_AMY + MEANING_DESC + PHONE_DESC_BOB
                + ADDRESS_DESC_BOB + TAG_DESC_FRIEND + TAG_DESC_HUSBAND;
        editedWord = new WordBuilder(BOB).withName(VALID_NAME_AMY).build();
        assertCommandSuccess(command, index, editedWord);

        /* Case: edit a word with new values same as another word's values but with different phone
         * -> edited
         */
        index = INDEX_SECOND_WORD;
        command = EditCommand.COMMAND_WORD + " " + index.getOneBased() + NAME_DESC_BOB + MEANING_DESC + PHONE_DESC_AMY
                + ADDRESS_DESC_BOB + TAG_DESC_FRIEND + TAG_DESC_HUSBAND;
        editedWord = new WordBuilder(BOB).withPhone(VALID_PHONE_AMY).build();
        assertCommandSuccess(command, index, editedWord);

        /* Case: clear tags -> cleared */
        index = INDEX_FIRST_WORD;
        command = EditCommand.COMMAND_WORD + " " + index.getOneBased() + " " + PREFIX_TAG.getPrefix();
        Word wordToEdit = getModel().getFilteredWordList().get(index.getZeroBased());
        editedWord = new WordBuilder(wordToEdit).withTags().build();
        assertCommandSuccess(command, index, editedWord);

        /* ------------------ Performing edit operation while a filtered list is being shown ------------------------ */

        /* Case: filtered word list, edit index within bounds of learnvocabulary book and word list -> edited */
        showWordsWithName(KEYWORD_MATCHING_MEIER);
        index = INDEX_FIRST_WORD;
        assertTrue(index.getZeroBased() < getModel().getFilteredWordList().size());
        command = EditCommand.COMMAND_WORD + " " + index.getOneBased() + " " + NAME_DESC_BOB;
        wordToEdit = getModel().getFilteredWordList().get(index.getZeroBased());
        editedWord = new WordBuilder(wordToEdit).withName(VALID_NAME_BOB).build();
        assertCommandSuccess(command, index, editedWord);

        /* Case: filtered word list, edit index within bounds of learnvocabulary book but out of bounds of word list
         * -> rejected
         */
        showWordsWithName(KEYWORD_MATCHING_MEIER);
        int invalidIndex = getModel().getLearnVocabulary().getWordList().size();
        assertCommandFailure(EditCommand.COMMAND_WORD + " " + invalidIndex + NAME_DESC_BOB,
                Messages.MESSAGE_INVALID_WORDS_DISPLAYED_INDEX);

        /* --------------------- Performing edit operation while a word card is selected -------------------------- */

        /* Case: selects first card in the word list, edit a word -> edited, card selection remains unchanged but
         * browser url changes
         */
        showAllWords();
        index = INDEX_FIRST_WORD;
        selectWord(index);
        command = EditCommand.COMMAND_WORD + " " + index.getOneBased() + NAME_DESC_AMY + MEANING_DESC + PHONE_DESC_AMY
                + ADDRESS_DESC_AMY + TAG_DESC_FRIEND;
        // this can be misleading: card selection actually remains unchanged but the
        // browser's url is updated to reflect the new word's name
        assertCommandSuccess(command, index, AMY, index);

        /* --------------------------------- Performing invalid edit operation -------------------------------------- */

        /* Case: invalid index (0) -> rejected */
        assertCommandFailure(EditCommand.COMMAND_WORD + " 0" + NAME_DESC_BOB,
                String.format(Messages.MESSAGE_INVALID_COMMAND_FORMAT, EditCommand.MESSAGE_USAGE));

        /* Case: invalid index (-1) -> rejected */
        assertCommandFailure(EditCommand.COMMAND_WORD + " -1" + NAME_DESC_BOB,
                String.format(Messages.MESSAGE_INVALID_COMMAND_FORMAT, EditCommand.MESSAGE_USAGE));

        /* Case: invalid index (size + 1) -> rejected */
        invalidIndex = getModel().getFilteredWordList().size() + 1;
        assertCommandFailure(EditCommand.COMMAND_WORD + " " + invalidIndex + NAME_DESC_BOB,
                Messages.MESSAGE_INVALID_WORDS_DISPLAYED_INDEX);

        /* Case: missing index -> rejected */
        assertCommandFailure(EditCommand.COMMAND_WORD + NAME_DESC_BOB,
                String.format(Messages.MESSAGE_INVALID_COMMAND_FORMAT, EditCommand.MESSAGE_USAGE));

        /* Case: missing all fields -> rejected */
        assertCommandFailure(EditCommand.COMMAND_WORD + " " + INDEX_FIRST_WORD.getOneBased(),
                EditCommand.MESSAGE_NOT_EDITED);

        /* Case: invalid name -> rejected */
        assertCommandFailure(EditCommand.COMMAND_WORD + " " + INDEX_FIRST_WORD.getOneBased() + INVALID_NAME_DESC,
                Name.MESSAGE_NAME_CONSTRAINTS);

        /* Case: invalid phone -> rejected */
        assertCommandFailure(EditCommand.COMMAND_WORD + " " + INDEX_FIRST_WORD.getOneBased() + INVALID_PHONE_DESC,
                Phone.MESSAGE_PHONE_CONSTRAINTS);

        /* Case: invalid learnvocabulary -> rejected */
        assertCommandFailure(EditCommand.COMMAND_WORD + " " + INDEX_FIRST_WORD.getOneBased() + INVALID_ADDRESS_DESC,
                Address.MESSAGE_ADDRESS_CONSTRAINTS);

        /* Case: invalid tag -> rejected */
        assertCommandFailure(EditCommand.COMMAND_WORD + " " + INDEX_FIRST_WORD.getOneBased() + INVALID_TAG_DESC,
                Tag.MESSAGE_TAG_CONSTRAINTS);

        /* Case: edit a word with new values same as another word's values -> rejected */
        executeCommand(WordUtil.getAddCommand(BOB));
        assertTrue(getModel().getLearnVocabulary().getWordList().contains(BOB));
        index = INDEX_FIRST_WORD;
        assertFalse(getModel().getFilteredWordList().get(index.getZeroBased()).equals(BOB));
        command = EditCommand.COMMAND_WORD + " " + index.getOneBased() + NAME_DESC_BOB + MEANING_DESC + PHONE_DESC_BOB
                + ADDRESS_DESC_BOB + TAG_DESC_FRIEND + TAG_DESC_HUSBAND;
        assertCommandFailure(command, EditCommand.MESSAGE_DUPLICATE_WORD);

        /* Case: edit a word with new values same as another word's values but with different tags -> rejected */
        command = EditCommand.COMMAND_WORD + " " + index.getOneBased() + NAME_DESC_BOB + MEANING_DESC + PHONE_DESC_BOB
                + ADDRESS_DESC_BOB + TAG_DESC_HUSBAND;
        assertCommandFailure(command, EditCommand.MESSAGE_DUPLICATE_WORD);

        /* Case: edit a word with new values same as another
        word's values but with different learnvocabulary -> rejected */
        command = EditCommand.COMMAND_WORD + " " + index.getOneBased() + NAME_DESC_BOB + MEANING_DESC + PHONE_DESC_BOB
                + ADDRESS_DESC_AMY + TAG_DESC_FRIEND + TAG_DESC_HUSBAND;
        assertCommandFailure(command, EditCommand.MESSAGE_DUPLICATE_WORD);

        /* Case: edit a word with new values same as another word's values but with different phone -> rejected */
        command = EditCommand.COMMAND_WORD + " " + index.getOneBased() + NAME_DESC_BOB + MEANING_DESC + PHONE_DESC_AMY
                + ADDRESS_DESC_BOB + TAG_DESC_FRIEND + TAG_DESC_HUSBAND;
        assertCommandFailure(command, EditCommand.MESSAGE_DUPLICATE_WORD);

    }

    /**
     * Performs the same verification as {@code assertCommandSuccess(String, Index, Word, Index)} except that
     * the browser url and selected card remain unchanged.
     * @param toEdit the index of the current model's filtered list
     * @see EditCommandSystemTest#assertCommandSuccess(String, Index, Word, Index)
     */
    private void assertCommandSuccess(String command, Index toEdit, Word editedWord) {
        assertCommandSuccess(command, toEdit, editedWord, null);
    }

    /**
     * Performs the same verification as {@code assertCommandSuccess(String, Model, String, Index)} and in addition,<br>
     * 1. Asserts that result display box displays the success message of executing {@code EditCommand}.<br>
     * 2. Asserts that the model related components are updated to reflect the word at index {@code toEdit} being
     * updated to values specified {@code editedWord}.<br>
     * @param toEdit the index of the current model's filtered list.
     * @see EditCommandSystemTest#assertCommandSuccess(String, Model, String, Index)
     */
    private void assertCommandSuccess(String command, Index toEdit, Word editedWord,
            Index expectedSelectedCardIndex) {
        Model expectedModel = getModel();
        expectedModel.updateWord(expectedModel.getFilteredWordList().get(toEdit.getZeroBased()), editedWord);
        expectedModel.updateFilteredWordList(PREDICATE_SHOW_ALL_WORDS);

        assertCommandSuccess(command, expectedModel,
                String.format(EditCommand.MESSAGE_EDIT_WORD_SUCCESS, editedWord), expectedSelectedCardIndex);
    }

    /**
     * Performs the same verification as {@code assertCommandSuccess(String, Model, String, Index)} except that the
     * browser url and selected card remain unchanged.
     * @see EditCommandSystemTest#assertCommandSuccess(String, Model, String, Index)
     */
    private void assertCommandSuccess(String command, Model expectedModel, String expectedResultMessage) {
        assertCommandSuccess(command, expectedModel, expectedResultMessage, null);
    }

    /**
     * Executes {@code command} and in addition,<br>
     * 1. Asserts that the command box displays an empty string.<br>
     * 2. Asserts that the result display box displays {@code expectedResultMessage}.<br>
     * 3. Asserts that the browser url and selected card update accordingly depending on the card at
     * {@code expectedSelectedCardIndex}.<br>
     * 4. Asserts that the status bar's sync status changes.<br>
     * 5. Asserts that the command box has the default style class.<br>
     * Verifications 1 and 2 are performed by
     * {@code LearnVocabularySystemTest#assertApplicationDisplaysExpected(String, String, Model)}.<br>
     * @see LearnVocabularySystemTest#assertApplicationDisplaysExpected(String, String, Model)
     * @see LearnVocabularySystemTest#assertSelectedCardChanged(Index)
     */
    private void assertCommandSuccess(String command, Model expectedModel, String expectedResultMessage,
            Index expectedSelectedCardIndex) {
        executeCommand(command);
        expectedModel.updateFilteredWordList(PREDICATE_SHOW_ALL_WORDS);
        assertApplicationDisplaysExpected("", expectedResultMessage, expectedModel);
        assertCommandBoxShowsDefaultStyle();
        if (expectedSelectedCardIndex != null) {
            assertSelectedCardChanged(expectedSelectedCardIndex);
        } else {
            assertSelectedCardUnchanged();
        }
        assertStatusBarUnchangedExceptSyncStatus();
    }

    /**
     * Executes {@code command} and in addition,<br>
     * 1. Asserts that the command box displays {@code command}.<br>
     * 2. Asserts that result display box displays {@code expectedResultMessage}.<br>
     * 3. Asserts that the browser url, selected card and status bar remain unchanged.<br>
     * 4. Asserts that the command box has the error style.<br>
     * Verifications 1 and 2 are performed by
     * {@code LearnVocabularySystemTest#assertApplicationDisplaysExpected(String, String, Model)}.<br>
     * @see LearnVocabularySystemTest#assertApplicationDisplaysExpected(String, String, Model)
     */
    private void assertCommandFailure(String command, String expectedResultMessage) {
        Model expectedModel = getModel();

        executeCommand(command);
        assertApplicationDisplaysExpected(command, expectedResultMessage, expectedModel);
        assertSelectedCardUnchanged();
        assertCommandBoxShowsErrorStyle();
        assertStatusBarUnchanged();
    }
}
