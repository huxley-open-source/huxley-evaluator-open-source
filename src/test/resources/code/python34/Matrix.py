pontos = 0
pergunta1  = "Você acredita em destino?"
respostap1 = "nao"
pergunta2  = "Você sentiu a vida inteira que há algo errado com o mundo?"
respostap2 = "sim"
pergunta3  = "Voce ja teve um sonho que parecesse realidade?"
respostap3 = "sim"
pergunta4  = "voce quer saber a verdade?"
respostap4 = "sim"
pergunta5  = "Voce quer saber o que e matrix?"
respostap5 = "sim"

print(pergunta1)
resposta1 = input()
if resposta1 == respostap1:
    pontos = pontos + 2

print(pergunta2)
resposta2 = input()
if resposta2 == respostap2:
    pontos = pontos + 1

print(pergunta3)
resposta3 = input()
if resposta3 == respostap3:
    pontos = pontos + 1

print(pergunta4)
resposta4 = input()
if resposta4 == respostap4:
    pontos = pontos + 1

print(pergunta5)
resposta5 = input()
if resposta5 == respostap5:
    pontos = pontos + 3 

    if pontos  >=5:
        print ("A Matrix esta em todo lugar. A nossa volta. E o mundo colocado diante de seus olhos, para que nao veja a verdade. Infelizmente a impossivel dizer o que a a Matrix. Você tem de ver por si mesmo. *Voce a sugado pelo telefone e revelado a verdade …")
    else:
        print ("")

