class CreatePurchases < ActiveRecord::Migration
  def change
    create_table :purchases do |t|
      t.integer :typ
      t.integer :typ_id
      t.integer :repository_id

      t.timestamps
    end
  end
end
