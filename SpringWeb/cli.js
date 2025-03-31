const readline = require('readline');

const rl = readline.createInterface({
  input: process.stdin,
  output: process.stdout
});

function showMenu() {
  console.log('1. Seçenek 1');
  console.log('2. Seçenek 2');
  console.log('3. Seçenek 3');
  console.log('4. Çıkış');
}

function handleMenu(choice) {
  switch (choice) {
    case '1':
      console.log('Seçenek 1 seçildi.');
      break;
    case '2':
      console.log('Seçenek 2 seçildi.');
      break;
    case '3':
      console.log('Seçenek 3 seçildi.');
      break;
    case '4':
      console.log('Çıkış yapılıyor...');
      rl.close();
      return;
    default:
      console.log('Geçersiz seçenek. Lütfen tekrar deneyin.');
  }
  askMenu();
}

function askMenu() {
  rl.question('Bir seçenek girin: ', (choice) => {
    handleMenu(choice);
  });
}

// Menüyü başlat
showMenu();
askMenu(); 