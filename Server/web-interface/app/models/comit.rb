class Comit < ActiveRecord::Base
  attr_accessible :comit_hash, :is_update, :message, :repository_id, :time
  belongs_to :repository
  has_many :fils, :dependent => :destroy

  validates :comit_hash, :presence => true
  validates :time, :presence => true
  validates :is_update, :inclusion => { :in => [ true, false ] }
  validate :repository_validity

  def repository_validity
    if !repository_id || !Repository.exists?(repository_id)
      errors.add(:repository_id, "Please choose an appropriate Repository for the commit.")
    end
  end
end
