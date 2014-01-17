class CreateInstallers < ActiveRecord::Migration
  def change
    create_table :installers do |t|
      t.string :name
      t.string :username
      t.string :password_digest
      t.text :contact

      t.timestamps
    end
  end
end
