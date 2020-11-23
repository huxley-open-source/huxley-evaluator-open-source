program sejabemvindo_correct;
var nome : string;

procedure overflow();
begin
    overflow();
end;

begin
    readln(nome);
    overflow();
    write('Seja muito bem-vindo ');
    writeln(nome);
end.