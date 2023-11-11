using System;

using System.Windows;


namespace WpfApp2
{
    public partial class MainWindow : Window
    {
        public MainWindow(string question, string defaultAnswer = "")
        {
            InitializeComponent();
            lbQuestion.Content = question;
            txtAnswer.Text = defaultAnswer;
        }

        private void btnDialogOk_Click(object sender, RoutedEventArgs e)
        {
            this.DialogResult = true;
        }

        private void Window_ContentRendered(object sender, EventArgs e)
        {
            txtAnswer.SelectAll();
            txtAnswer.Focus();
        }

        public string Answer
        {
            get { return txtAnswer.Text; }
        }
    }
}