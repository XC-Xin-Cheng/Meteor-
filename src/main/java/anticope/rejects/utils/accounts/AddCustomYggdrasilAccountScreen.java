package anticope.rejects.utils.accounts;

import meteordevelopment.meteorclient.gui.GuiTheme;
import meteordevelopment.meteorclient.gui.screens.accounts.AccountsScreen;
import meteordevelopment.meteorclient.gui.screens.accounts.AddAccountScreen;
import meteordevelopment.meteorclient.gui.widgets.containers.WTable;
import meteordevelopment.meteorclient.gui.widgets.input.WTextBox;
import meteordevelopment.meteorclient.gui.widgets.pressable.WButton;
import meteordevelopment.meteorclient.systems.accounts.Accounts;

public class AddCustomYggdrasilAccountScreen extends AddAccountScreen {
   public AddCustomYggdrasilAccountScreen(GuiTheme theme, AccountsScreen parent) {
      super(theme, "Add Yggdrasil Account", parent);
   }

   public void initWidgets() {
      WTable t = (WTable)this.add(this.theme.table()).widget();
      t.add(this.theme.label("Username / Email: "));
      WTextBox username = (WTextBox)t.add(this.theme.textBox("")).minWidth(400.0).expandX().widget();
      username.setFocused(true);
      t.row();
      t.add(this.theme.label("Password: "));
      WTextBox password = (WTextBox)t.add(this.theme.textBox("")).minWidth(400.0).expandX().widget();
      t.row();
      t.add(this.theme.label("Server: "));
      WTextBox server = (WTextBox)t.add(this.theme.textBox("")).minWidth(400.0).expandX().widget();
      t.row();
      this.add = (WButton)t.add(this.theme.button("Add")).expandX().widget();
      this.add.action = () -> {
         CustomYggdrasilAccount account = new CustomYggdrasilAccount(username.get(), password.get(), server.get());
         if (!username.get().isEmpty() && !password.get().isEmpty() && !Accounts.get().exists(account)) {
            AccountsScreen.addAccount(this, this.parent, account);
         }
      };
      this.enterAction = this.add.action;
   }
}
